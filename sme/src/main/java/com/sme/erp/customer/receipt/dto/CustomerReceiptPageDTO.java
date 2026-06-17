package com.sme.erp.customer.receipt.dto;

import java.util.List;

public class CustomerReceiptPageDTO {
    private List<CustomerReceiptDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public CustomerReceiptPageDTO(List<CustomerReceiptDTO> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<CustomerReceiptDTO> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
