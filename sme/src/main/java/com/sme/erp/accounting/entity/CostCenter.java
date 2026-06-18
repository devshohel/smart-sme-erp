package com.sme.erp.accounting.entity;

import com.sme.erp.enums.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="accounting_cost_centers")
public class CostCenter {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String code;
 @Column(nullable=false) private String name;
 private String description;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.ACTIVE;
 private LocalDateTime createdAt; private LocalDateTime updatedAt;
 @PrePersist void create(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getCode(){return code;} public void setCode(String v){code=v;}
 public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
