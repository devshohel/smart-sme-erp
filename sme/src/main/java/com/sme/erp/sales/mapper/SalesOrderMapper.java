package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesOrderDTO;
import com.sme.erp.sales.entity.SalesOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SalesOrderMapper {

    public SalesOrderDTO toDTO(SalesOrder entity) {
        if (entity == null) {
            return null;
        }

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setId(entity.getId());
        dto.setOrderNo(entity.getOrderNo());
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        dto.setOrderDate(entity.getOrderDate());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setGrandTotal(BigDecimal.ZERO);
        return dto;
    }
}
