package com.sme.erp.accounting.entity;

import com.sme.erp.accounting.enums.JournalStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounting_journal_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_accounting_journal_source", columnNames = {"source_type", "source_id"}))
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String journalNo;

    @Column(nullable = false)
    private LocalDate journalDate;

    private String referenceNo;
    private String referenceType;
    private String description;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_no")
    private String sourceNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalStatus status = JournalStatus.DRAFT;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime postedAt;
    private LocalDateTime cancelledAt;
    private String createdBy;
    private String postedBy;
    private String cancelledBy;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntryLine> lines = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        recalculateTotals();
        if (status == JournalStatus.POSTED && postedAt == null) postedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalculateTotals();
    }

    private void recalculateTotals() {
        totalDebit = lines.stream().map(JournalEntryLine::getDebit).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCredit = lines.stream().map(JournalEntryLine::getCredit).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addLine(JournalEntryLine line) {
        lines.add(line);
        line.setJournalEntry(this);
    }

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
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
    public List<JournalEntryLine> getLines() { return lines; }
    public void setLines(List<JournalEntryLine> lines) { this.lines = lines; }
}
