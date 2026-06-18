package com.sme.erp.accounting.entity;
import com.sme.erp.accounting.enums.YearEndClosingStatus; import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*;
@Entity @Table(name="accounting_year_end_closings") public class YearEndClosing {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false) private Integer fiscalYear; @Column(nullable=false) private LocalDate closingDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private YearEndClosingStatus status=YearEndClosingStatus.DRAFT;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal retainedEarningsAmount=BigDecimal.ZERO; private Long closingJournalId;
 private LocalDateTime createdAt; private LocalDateTime completedAt; private String completedBy; @PrePersist void create(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;} public Integer getFiscalYear(){return fiscalYear;} public void setFiscalYear(Integer v){fiscalYear=v;}
 public LocalDate getClosingDate(){return closingDate;} public void setClosingDate(LocalDate v){closingDate=v;} public YearEndClosingStatus getStatus(){return status;} public void setStatus(YearEndClosingStatus v){status=v;}
 public BigDecimal getRetainedEarningsAmount(){return retainedEarningsAmount;} public void setRetainedEarningsAmount(BigDecimal v){retainedEarningsAmount=v;} public Long getClosingJournalId(){return closingJournalId;} public void setClosingJournalId(Long v){closingJournalId=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;} public String getCompletedBy(){return completedBy;} public void setCompletedBy(String v){completedBy=v;}
}
