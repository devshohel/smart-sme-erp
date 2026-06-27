package com.sme.erp.sales.pos.service.impl;

import com.sme.erp.common.exception.BadRequestException;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.receipt.dto.CustomerReceiptAllocationDTO;
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
import com.sme.erp.sales.dto.SalesItemDTO;
import com.sme.erp.sales.enums.SalesInvoiceStatus;
import com.sme.erp.sales.pos.dto.PosCompleteRequestDTO;
import com.sme.erp.sales.pos.dto.PosCompleteResponseDTO;
import com.sme.erp.sales.pos.dto.PosItemRequestDTO;
import com.sme.erp.sales.pos.enums.PosPaymentMethod;
import com.sme.erp.sales.pos.service.PosService;
import com.sme.erp.sales.service.SalesInvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PosServiceImpl implements PosService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SalesInvoiceService salesInvoiceService;
    private final CustomerReceiptService customerReceiptService;
    private final CustomerService customerService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public PosServiceImpl(SalesInvoiceService salesInvoiceService,
                          CustomerReceiptService customerReceiptService,
                          CustomerService customerService,
                          WarehouseService warehouseService,
                          ProductService productService) {
        this.salesInvoiceService = salesInvoiceService;
        this.customerReceiptService = customerReceiptService;
        this.customerService = customerService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @Override
    @Transactional
    public PosCompleteResponseDTO complete(PosCompleteRequestDTO request) {
        validateRequest(request);

        CustomerDTO customer = customerService.getById(request.getCustomerId());
        if (customer.getStatus() != null && customer.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Selected customer is not active");
        }

        WarehouseDTO warehouse = warehouseService.getById(request.getWarehouseId());
        if (Boolean.FALSE.equals(warehouse.getActive())) {
            throw new BadRequestException("Selected warehouse is not active");
        }

        SalesInvoiceDTO invoiceRequest = buildInvoiceRequest(request);
        SalesInvoiceDTO created = salesInvoiceService.create(invoiceRequest);
        BigDecimal paidAmount = money(request.getPayment().getPaidAmount());
        if (paidAmount.compareTo(safe(created.getNetTotal())) > 0) {
            throw new BadRequestException("Paid amount cannot exceed the backend-calculated grand total");
        }
        if (request.getPayment().getPaymentMethod() == PosPaymentMethod.DUE && paidAmount.signum() > 0) {
            throw new BadRequestException("Due payment method requires a zero paid amount");
        }

        salesInvoiceService.submit(created.getId());
        salesInvoiceService.approve(created.getId());
        SalesInvoiceDTO posted = salesInvoiceService.post(created.getId());

        CustomerReceiptDTO receipt = null;
        if (paidAmount.signum() > 0) {
            receipt = createAndPostReceipt(request, posted, paidAmount);
        }

        SalesInvoiceDTO completed = salesInvoiceService.getById(posted.getId());
        return toResponse(completed, request.getPayment().getPaymentMethod(), receipt);
    }

    private SalesInvoiceDTO buildInvoiceRequest(PosCompleteRequestDTO request) {
        List<SalesItemDTO> items = new ArrayList<>();
        for (PosItemRequestDTO requestedItem : request.getItems()) {
            if (requestedItem == null || requestedItem.getProductId() == null
                    || requestedItem.getQuantity() == null || requestedItem.getQuantity().signum() <= 0) {
                throw new BadRequestException("Every POS item requires a valid product and positive quantity");
            }
            ProductDTO product = productService.getById(requestedItem.getProductId());
            if (product.getStatus() != null && product.getStatus() != Status.ACTIVE) {
                throw new BadRequestException("Product is not active: " + product.getProductName());
            }

            BigDecimal quantity = requestedItem.getQuantity();
            BigDecimal unitPrice = money(product.getSalePrice());
            if (unitPrice.signum() < 0) {
                throw new BadRequestException("Product sale price cannot be negative: " + product.getProductName());
            }
            BigDecimal gross = money(quantity.multiply(unitPrice));
            BigDecimal discount = money(requestedItem.getDiscount());
            if (discount.compareTo(gross) > 0) {
                throw new BadRequestException("Discount cannot exceed line gross for product: " + product.getProductName());
            }
            BigDecimal taxable = gross.subtract(discount);
            BigDecimal taxRate = safe(product.getTaxPercentage());
            if (taxRate.signum() < 0) {
                throw new BadRequestException("Product tax percentage cannot be negative: " + product.getProductName());
            }
            BigDecimal tax = money(taxable.multiply(taxRate).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));

            SalesItemDTO item = new SalesItemDTO();
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setUomId(product.getUomId());
            item.setUomName(product.getUomName());
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setDiscount(discount);
            item.setTax(tax);
            item.setSubTotal(gross.subtract(discount).add(tax));
            items.add(item);
        }

        SalesInvoiceDTO invoice = new SalesInvoiceDTO();
        invoice.setCustomerId(request.getCustomerId());
        invoice.setWarehouseId(request.getWarehouseId());
        invoice.setSaleDate(request.getSaleDate());
        invoice.setNotes(request.getNotes());
        invoice.setStatus(SalesInvoiceStatus.DRAFT);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setItems(items);
        return invoice;
    }

    private CustomerReceiptDTO createAndPostReceipt(PosCompleteRequestDTO request,
                                                     SalesInvoiceDTO invoice,
                                                     BigDecimal paidAmount) {
        CustomerReceiptAllocationDTO allocation = new CustomerReceiptAllocationDTO();
        allocation.setSalesInvoiceId(invoice.getId());
        allocation.setAllocatedAmount(paidAmount);

        CustomerReceiptDTO receiptRequest = new CustomerReceiptDTO();
        receiptRequest.setCustomerId(request.getCustomerId());
        receiptRequest.setReceiptDate(request.getSaleDate().toLocalDate());
        receiptRequest.setPaymentMethod(toReceiptPaymentMethod(request.getPayment().getPaymentMethod()));
        receiptRequest.setAmount(paidAmount);
        receiptRequest.setReferenceNo(request.getPayment().getReferenceNo());
        receiptRequest.setNotes("POS payment for invoice " + invoice.getInvoiceNo());
        receiptRequest.setAllocationMode(CustomerReceiptAllocationMode.MANUAL);
        receiptRequest.setAllocations(List.of(allocation));

        CustomerReceiptDTO createdReceipt = customerReceiptService.create(receiptRequest);
        return customerReceiptService.post(createdReceipt.getId());
    }

    private PosCompleteResponseDTO toResponse(SalesInvoiceDTO invoice,
                                              PosPaymentMethod paymentMethod,
                                              CustomerReceiptDTO receipt) {
        PosCompleteResponseDTO response = new PosCompleteResponseDTO();
        response.setInvoiceId(invoice.getId());
        response.setInvoiceNo(invoice.getInvoiceNo());
        response.setCustomerId(invoice.getCustomerId());
        response.setCustomerName(invoice.getCustomerName());
        response.setWarehouseId(invoice.getWarehouseId());
        response.setWarehouseName(invoice.getWarehouseName());
        response.setItems(invoice.getItems());
        response.setSubtotal(invoice.getTotalAmount());
        response.setDiscountAmount(invoice.getDiscountAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setGrandTotal(invoice.getNetTotal());
        response.setPaidAmount(invoice.getPaidAmount());
        response.setDueAmount(invoice.getDueAmount());
        response.setPaymentMethod(paymentMethod);
        response.setStatus(invoice.getStatus());
        response.setSaleDate(invoice.getSaleDate());
        if (receipt != null) {
            response.setReceiptId(receipt.getId());
            response.setReceiptNo(receipt.getReceiptNo());
        }
        return response;
    }

    private CustomerReceiptPaymentMethod toReceiptPaymentMethod(PosPaymentMethod method) {
        return switch (method) {
            case CASH -> CustomerReceiptPaymentMethod.CASH;
            case CARD, BANK -> CustomerReceiptPaymentMethod.BANK;
            case MOBILE_BANKING -> CustomerReceiptPaymentMethod.MOBILE_BANKING;
            case OTHER -> CustomerReceiptPaymentMethod.OTHER;
            case DUE -> throw new BadRequestException("Due sales do not create a customer receipt");
        };
    }

    private void validateRequest(PosCompleteRequestDTO request) {
        if (request == null) {
            throw new BadRequestException("POS request is required");
        }
        if (request.getCustomerId() == null || request.getCustomerId() <= 0
                || request.getWarehouseId() == null || request.getWarehouseId() <= 0
                || request.getSaleDate() == null) {
            throw new BadRequestException("Customer, warehouse, and sale date are required");
        }
        if (request.getPayment() == null || request.getPayment().getPaymentMethod() == null) {
            throw new BadRequestException("POS payment details are required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("At least one sale item is required");
        }
        if (request.getPayment().getPaidAmount() == null || request.getPayment().getPaidAmount().signum() < 0) {
            throw new BadRequestException("Paid amount cannot be negative");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
