package com.sme.erp.customer.dto;

import java.util.List;

public class CustomerPageDTO {
    private List<CustomerDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public CustomerPageDTO(List<CustomerDTO> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<CustomerDTO> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
