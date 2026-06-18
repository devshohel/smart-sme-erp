package com.sme.erp.accounting.dto;

import com.sme.erp.accounting.enums.JournalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JournalEntryDTO {
    private Long id;
    private String journalNo;
    @NotNull(message = "Journal date is required")
    private LocalDate journalDate;
    private String referenceNo;
    private String referenceType;
    private String description;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private JournalStatus status;
    @Valid
    private List<JournalEntryLineDTO> lines = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime postedAt;
    private LocalDateTime cancelledAt;
    private String createdBy;
    private String postedBy;
    private String cancelledBy;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJournalNo() { return journalNo; }
    public void setJournalNo(String journalNo) { this.journalNo = journalNo; }
    public LocalDate getJournalDate() { return journalDate; }
    public void setJournalDate(LocalDate journalDate) { this.journalDate = journalDate; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public JournalStatus getStatus() { return status; }
    public void setStatus(JournalStatus status) { this.status = status; }
    public List<JournalEntryLineDTO> getLines() { return lines; }
    public void setLines(List<JournalEntryLineDTO> lines) { this.lines = lines; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }
}
