package com.sme.erp.accounting.entity;
import com.sme.erp.accounting.enums.AccountingPeriodStatus; import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="accounting_periods") public class AccountingPeriod {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private String periodName;
 @Column(nullable=false) private LocalDate startDate; @Column(nullable=false) private LocalDate endDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private AccountingPeriodStatus status=AccountingPeriodStatus.OPEN;
 private LocalDateTime closedAt; private String closedBy; private String remarks;
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getPeriodName(){return periodName;} public void setPeriodName(String v){periodName=v;}
 public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;} public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
 public AccountingPeriodStatus getStatus(){return status;} public void setStatus(AccountingPeriodStatus v){status=v;} public LocalDateTime getClosedAt(){return closedAt;} public void setClosedAt(LocalDateTime v){closedAt=v;}
 public String getClosedBy(){return closedBy;} public void setClosedBy(String v){closedBy=v;} public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;}
}
