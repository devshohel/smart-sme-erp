package com.sme.erp.product.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.sme.erp.enums.Status;

@Entity
@Table(name = "product_brands", indexes = {
        @Index(name = "idx_brand_code", columnList = "code")
})
@SQLDelete(sql = "UPDATE product_brands SET is_deleted = true WHERE id=?")
@SQLRestriction("(is_deleted = false or is_deleted is null)")
public class ProductBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "is_deleted")
    private Boolean deleted = false;

    // 🕒 Audit Fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔄 Lifecycle Hooks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor
    public ProductBrand() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) {this.id = id;}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
