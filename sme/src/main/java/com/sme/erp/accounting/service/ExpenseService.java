package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.ExpenseDTO;
import com.sme.erp.accounting.dto.ExpensePageDTO;
import com.sme.erp.accounting.dto.ExpenseReportRowDTO;
import com.sme.erp.accounting.enums.AccountingPaymentMethod;
import com.sme.erp.accounting.enums.ExpenseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    List<ExpenseDTO> getAll(LocalDate fromDate, LocalDate toDate, Long categoryId, AccountingPaymentMethod paymentMethod);
    ExpensePageDTO searchPage(String keyword, LocalDate fromDate, LocalDate toDate, Long categoryId,
                              AccountingPaymentMethod paymentMethod, ExpenseStatus status,
                              int page, int size, String sort, String direction);
    List<ExpenseDTO> approvalQueue(LocalDate fromDate, LocalDate toDate, Long categoryId, String submittedBy,
                                   BigDecimal amountMin, BigDecimal amountMax);
    ExpenseDTO getById(Long id);
    ExpenseDTO create(ExpenseDTO dto);
    ExpenseDTO create(ExpenseDTO dto, MultipartFile receipt);
    ExpenseDTO update(Long id, ExpenseDTO dto);
    ExpenseDTO update(Long id, ExpenseDTO dto, MultipartFile receipt);
    ExpenseDTO submit(Long id);
    ExpenseDTO approve(Long id);
    ExpenseDTO reject(Long id, String reason);
    ExpenseDTO reverse(Long id, String reversalReason);
    ExpenseDTO cancel(Long id);
    ExpenseDTO post(Long id);
    List<ExpenseReportRowDTO> reportSummary(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status);
    List<ExpenseReportRowDTO> reportByCategory(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status);
    List<ExpenseReportRowDTO> reportByPaymentMethod(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status);
    List<ExpenseReportRowDTO> reportTax(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status);
    List<ExpenseReportRowDTO> reportMonthly(LocalDate fromDate, LocalDate toDate, Long categoryId, ExpenseStatus status);
}
