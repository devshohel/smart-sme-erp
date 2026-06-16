package com.sme.erp.product.dto;

import java.util.List;

public class ProductPageDTO {
    private List<ProductDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public ProductPageDTO(List<ProductDTO> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<ProductDTO> getContent() { return content; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
