package com.sme.erp.sales.mapper;

import com.sme.erp.sales.dto.SalesReturnDTO;
import com.sme.erp.sales.entity.SalesReturn;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SalesReturnMapper {

    private final SalesReturnItemMapper salesReturnItemMapper;

    public SalesReturnMapper(SalesReturnItemMapper salesReturnItemMapper) {
        this.salesReturnItemMapper = salesReturnItemMapper;
    }

    public SalesReturnDTO toDTO(SalesReturn entity) {
        if (entity == null) {
            return null;
        }

        SalesReturnDTO dto = new SalesReturnDTO();
        dto.setId(entity.getId());
        dto.setReturnCode(entity.getReturnCode());
        if (entity.getInvoice() != null) {
            dto.setInvoiceId(entity.getInvoice().getId());
            dto.setInvoiceNo(entity.getInvoice().getInvoiceNo());
        }
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        dto.setReturnDate(entity.getReturnDate());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setNotes(entity.getNotes());
        dto.setItems(entity.getItems().stream().map(salesReturnItemMapper::toDTO).collect(Collectors.toList()));
        return dto;
    }
}
