package com.sme.erp.supplier.dto;

import java.util.List;

public class SupplierPageDTO {
    private List<SupplierDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public SupplierPageDTO(List<SupplierDTO> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<SupplierDTO> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
