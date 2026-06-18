package com.sme.erp.accounting.dto;
import com.sme.erp.enums.Status; import java.time.LocalDateTime;
public record CostCenterDTO(Long id,String code,String name,String description,Status status,LocalDateTime createdAt,LocalDateTime updatedAt){}
