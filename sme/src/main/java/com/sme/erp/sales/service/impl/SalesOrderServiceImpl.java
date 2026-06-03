package com.sme.erp.sales.service.impl;

import com.sme.erp.common.exception.DuplicateResourceException;
import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.common.util.RequestValueUtils;
import com.sme.erp.customer.entity.Customer;
import com.sme.erp.customer.repository.CustomerRepository;
import com.sme.erp.inventory.entity.Warehouse;
import com.sme.erp.inventory.repository.WarehouseRepository;
import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesOrder;
import com.sme.erp.sales.mapper.SalesOrderMapper;
import com.sme.erp.sales.repository.SalesOrderRepository;
import com.sme.erp.sales.service.SalesOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderMapper salesOrderMapper;

    public SalesOrderServiceImpl(
            SalesOrderRepository salesOrderRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            SalesOrderMapper salesOrderMapper) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderMapper = salesOrderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDTO> getAll() {
        return salesOrderRepository.findAll().stream()
                .map(salesOrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getById(Long id) {
        return salesOrderMapper.toDTO(findOrderById(id));
    }

    @Override
    @Transactional
    public SalesOrderDTO create(SalesOrderDTO dto) {
        SalesOrder entity = new SalesOrder();
        return save(dto, entity);
    }

    @Override
    @Transactional
    public SalesOrderDTO update(Long id, SalesOrderDTO dto) {
        SalesOrder entity = findOrderById(id);
        return save(dto, entity);
    }

    private SalesOrderDTO save(SalesOrderDTO dto, SalesOrder entity) {
        String requestedOrderNo = RequestValueUtils.normalize(dto.getOrderNo());
        entity.setOrderNo(resolveOrderNo(entity.getId(), requestedOrderNo));
        entity.setCustomer(findCustomerById(dto.getCustomerId()));
        entity.setWarehouse(findWarehouseById(dto.getWarehouseId()));
        entity.setOrderDate(dto.getOrderDate());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
        entity.setCreatedBy(dto.getCreatedBy());

        SalesOrder saved = salesOrderRepository.save(entity);
        SalesOrderDTO response = salesOrderMapper.toDTO(saved);
        response.setItems(dto.getItems());
        response.setGrandTotal(dto.getGrandTotal());
        response.setNotes(dto.getNotes());
        return response;
    }

    private String resolveOrderNo(Long currentId, String requestedOrderNo) {
        if (requestedOrderNo != null) {
            validateOrderNoUnique(requestedOrderNo, currentId);
            return requestedOrderNo;
        }

        if (currentId != null) {
            return findOrderById(currentId).getOrderNo();
        }

        long nextNumber = salesOrderRepository.findTopByOrderByIdDesc()
                .map(order -> order.getId() + 1)
                .orElse(1L);
        String generated = String.format("SO-%04d", nextNumber);
        while (salesOrderRepository.existsByOrderNo(generated)) {
            nextNumber++;
            generated = String.format("SO-%04d", nextNumber);
        }
        return generated;
    }

    private void validateOrderNoUnique(String orderNo, Long currentId) {
        boolean exists = currentId == null
                ? salesOrderRepository.existsByOrderNo(orderNo)
                : salesOrderRepository.existsByOrderNoAndIdNot(orderNo, currentId);
        if (exists) {
            throw new DuplicateResourceException("Sales order number already exists: " + orderNo);
        }
    }

    private SalesOrder findOrderById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found with id: " + id));
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }
}
