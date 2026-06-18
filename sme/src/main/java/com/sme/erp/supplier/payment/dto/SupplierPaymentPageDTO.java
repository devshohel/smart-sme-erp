package com.sme.erp.supplier.payment.dto;

import java.util.List;

public class SupplierPaymentPageDTO {
    private List<SupplierPaymentDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public SupplierPaymentPageDTO(List<SupplierPaymentDTO> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<SupplierPaymentDTO> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
