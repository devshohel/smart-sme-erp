package com.sme.erp.sales.pos.service;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptDTO;
import com.sme.erp.customer.receipt.enums.CustomerReceiptAllocationMode;
import com.sme.erp.customer.receipt.enums.CustomerReceiptPaymentMethod;
import com.sme.erp.customer.receipt.service.CustomerReceiptService;
import com.sme.erp.customer.service.CustomerService;
import com.sme.erp.enums.Status;
import com.sme.erp.inventory.dto.WarehouseDTO;
import com.sme.erp.inventory.service.WarehouseService;
import com.sme.erp.product.dto.ProductDTO;
import com.sme.erp.product.service.ProductService;
import com.sme.erp.sales.dto.SalesInvoiceDTO;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.enums.SalesPaymentStatus;
import com.sme.erp.sales.pos.dto.PosCompleteRequestDTO;
import com.sme.erp.sales.pos.dto.PosCompleteResponseDTO;
import com.sme.erp.sales.pos.dto.PosItemRequestDTO;
import com.sme.erp.sales.pos.dto.PosPaymentRequestDTO;
import com.sme.erp.sales.pos.enums.PosPaymentMethod;
import com.sme.erp.sales.pos.service.impl.PosServiceImpl;
import com.sme.erp.sales.service.SalesInvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosServiceImplTest {
    @Mock private SalesInvoiceService salesInvoiceService;
    @Mock private CustomerReceiptService customerReceiptService;
    @Mock private CustomerService customerService;
    @Mock private WarehouseService warehouseService;
    @Mock private ProductService productService;

    private PosServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PosServiceImpl(
                salesInvoiceService,
                customerReceiptService,
                customerService,
                warehouseService,
                productService);
    }

    @Test
    void complete_usesMasterPriceAndTaxThenPostsAllocatedReceipt() {
        PosCompleteRequestDTO request = request(PosPaymentMethod.CASH, "50.00");
        stubMasterData();
        SalesInvoiceDTO created = invoice(7L, "209.00", "0.00", "209.00", SalesInvoiceStatus.DRAFT);
        SalesInvoiceDTO posted = invoice(7L, "209.00", "0.00", "209.00", SalesInvoiceStatus.POSTED);
        SalesInvoiceDTO completed = invoice(7L, "209.00", "50.00", "159.00", SalesInvoiceStatus.PARTIAL_PAID);
        when(salesInvoiceService.create(any())).thenReturn(created);
        when(salesInvoiceService.post(7L)).thenReturn(posted);
        when(salesInvoiceService.getById(7L)).thenReturn(completed);

        CustomerReceiptDTO createdReceipt = new CustomerReceiptDTO();
        createdReceipt.setId(9L);
        when(customerReceiptService.create(any())).thenReturn(createdReceipt);
        CustomerReceiptDTO postedReceipt = new CustomerReceiptDTO();
        postedReceipt.setId(9L);
        postedReceipt.setReceiptNo("CR-000009");
        when(customerReceiptService.post(9L)).thenReturn(postedReceipt);

        PosCompleteResponseDTO response = service.complete(request);

        ArgumentCaptor<SalesInvoiceDTO> invoiceCaptor = ArgumentCaptor.forClass(SalesInvoiceDTO.class);
        verify(salesInvoiceService).create(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getItems().get(0).getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(invoiceCaptor.getValue().getItems().get(0).getDiscount()).isEqualByComparingTo("10.00");
        assertThat(invoiceCaptor.getValue().getItems().get(0).getTax()).isEqualByComparingTo("19.00");
        verify(salesInvoiceService).submit(7L);
        verify(salesInvoiceService).approve(7L);
        verify(salesInvoiceService).post(7L);

        ArgumentCaptor<CustomerReceiptDTO> receiptCaptor = ArgumentCaptor.forClass(CustomerReceiptDTO.class);
        verify(customerReceiptService).create(receiptCaptor.capture());
        CustomerReceiptDTO receipt = receiptCaptor.getValue();
        assertThat(receipt.getPaymentMethod()).isEqualTo(CustomerReceiptPaymentMethod.CASH);
        assertThat(receipt.getAllocationMode()).isEqualTo(CustomerReceiptAllocationMode.MANUAL);
        assertThat(receipt.getAllocations()).singleElement().satisfies(allocation -> {
            assertThat(allocation.getSalesInvoiceId()).isEqualTo(7L);
            assertThat(allocation.getAllocatedAmount()).isEqualByComparingTo("50.00");
        });
        assertThat(response.getPaidAmount()).isEqualByComparingTo("50.00");
        assertThat(response.getDueAmount()).isEqualByComparingTo("159.00");
        assertThat(response.getReceiptNo()).isEqualTo("CR-000009");
    }

    @Test
    void complete_dueSalePostsInvoiceWithoutReceipt() {
        PosCompleteRequestDTO request = request(PosPaymentMethod.DUE, "0.00");
        stubMasterData();
        SalesInvoiceDTO created = invoice(8L, "209.00", "0.00", "209.00", SalesInvoiceStatus.DRAFT);
        SalesInvoiceDTO posted = invoice(8L, "209.00", "0.00", "209.00", SalesInvoiceStatus.POSTED);
        when(salesInvoiceService.create(any())).thenReturn(created);
        when(salesInvoiceService.post(8L)).thenReturn(posted);
        when(salesInvoiceService.getById(8L)).thenReturn(posted);

        PosCompleteResponseDTO response = service.complete(request);

        verify(customerReceiptService, never()).create(any());
        assertThat(response.getPaymentMethod()).isEqualTo(PosPaymentMethod.DUE);
        assertThat(response.getDueAmount()).isEqualByComparingTo("209.00");
    }

    @Test
    void complete_rejectsTenderAboveBackendTotalBeforePosting() {
        PosCompleteRequestDTO request = request(PosPaymentMethod.CASH, "210.00");
        stubMasterData();
        when(salesInvoiceService.create(any())).thenReturn(invoice(10L, "209.00", "0.00", "209.00", SalesInvoiceStatus.DRAFT));

        assertThatThrownBy(() -> service.complete(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Paid amount cannot exceed the backend-calculated grand total");

        verify(salesInvoiceService, never()).submit(any());
        verify(customerReceiptService, never()).create(any());
    }

    private void stubMasterData() {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setName("Walk-in Customer");
        customer.setStatus(Status.ACTIVE);
        when(customerService.getById(1L)).thenReturn(customer);

        WarehouseDTO warehouse = new WarehouseDTO();
        warehouse.setId(2L);
        warehouse.setName("Main Warehouse");
        warehouse.setActive(true);
        when(warehouseService.getById(2L)).thenReturn(warehouse);

        ProductDTO product = new ProductDTO();
        product.setId(3L);
        product.setProductName("Product A");
        product.setStatus(Status.ACTIVE);
        product.setSalePrice(new BigDecimal("100.00"));
        product.setTaxPercentage(new BigDecimal("10.00"));
        when(productService.getById(3L)).thenReturn(product);
    }

    private PosCompleteRequestDTO request(PosPaymentMethod method, String paidAmount) {
        PosItemRequestDTO item = new PosItemRequestDTO();
        item.setProductId(3L);
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(BigDecimal.ONE);
        item.setDiscount(new BigDecimal("10.00"));
        item.setTax(BigDecimal.ZERO);

        PosPaymentRequestDTO payment = new PosPaymentRequestDTO();
        payment.setPaymentMethod(method);
        payment.setPaidAmount(new BigDecimal(paidAmount));

        PosCompleteRequestDTO request = new PosCompleteRequestDTO();
        request.setCustomerId(1L);
        request.setWarehouseId(2L);
        request.setSaleDate(LocalDateTime.of(2026, 6, 28, 10, 30));
        request.setItems(List.of(item));
        request.setPayment(payment);
        return request;
    }

    private SalesInvoiceDTO invoice(Long id, String total, String paid, String due, SalesInvoiceStatus status) {
        SalesInvoiceDTO invoice = new SalesInvoiceDTO();
        invoice.setId(id);
        invoice.setInvoiceNo("INV-" + id);
        invoice.setCustomerId(1L);
        invoice.setCustomerName("Walk-in Customer");
        invoice.setWarehouseId(2L);
        invoice.setWarehouseName("Main Warehouse");
        invoice.setSaleDate(LocalDateTime.of(2026, 6, 28, 10, 30));
        invoice.setItems(List.of());
        invoice.setTotalAmount(new BigDecimal("200.00"));
        invoice.setDiscountAmount(new BigDecimal("10.00"));
        invoice.setTaxAmount(new BigDecimal("19.00"));
        invoice.setNetTotal(new BigDecimal(total));
        invoice.setPaidAmount(new BigDecimal(paid));
        invoice.setDueAmount(new BigDecimal(due));
        invoice.setPaymentStatus(new BigDecimal(due).signum() == 0 ? SalesPaymentStatus.PAID : new BigDecimal(paid).signum() > 0 ? SalesPaymentStatus.PARTIAL : SalesPaymentStatus.DUE);
        invoice.setStatus(status);
        return invoice;
    }
}
