package com.sme.erp.accounting.entity;
import com.sme.erp.accounting.enums.*; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*;
@Entity @Table(name="accounting_budgets") public class Budget {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String budgetNo;
 @Column(nullable=false) private Integer fiscalYear; @Enumerated(EnumType.STRING) @Column(nullable=false) private BudgetPeriodType periodType;
 @Column(nullable=false) private LocalDate fromDate; @Column(nullable=false) private LocalDate toDate;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="account_id",nullable=false) private Account account;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cost_center_id") private CostCenter costCenter;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private BudgetStatus status=BudgetStatus.DRAFT;
 private LocalDateTime createdAt; private LocalDateTime approvedAt; private String approvedBy;
 @PrePersist void create(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getBudgetNo(){return budgetNo;} public void setBudgetNo(String v){budgetNo=v;}
 public Integer getFiscalYear(){return fiscalYear;} public void setFiscalYear(Integer v){fiscalYear=v;} public BudgetPeriodType getPeriodType(){return periodType;} public void setPeriodType(BudgetPeriodType v){periodType=v;}
 public LocalDate getFromDate(){return fromDate;} public void setFromDate(LocalDate v){fromDate=v;} public LocalDate getToDate(){return toDate;} public void setToDate(LocalDate v){toDate=v;}
 public Account getAccount(){return account;} public void setAccount(Account v){account=v;} public CostCenter getCostCenter(){return costCenter;} public void setCostCenter(CostCenter v){costCenter=v;}
 public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;} public BudgetStatus getStatus(){return status;} public void setStatus(BudgetStatus v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getApprovedAt(){return approvedAt;} public void setApprovedAt(LocalDateTime v){approvedAt=v;} public String getApprovedBy(){return approvedBy;} public void setApprovedBy(String v){approvedBy=v;}
}
