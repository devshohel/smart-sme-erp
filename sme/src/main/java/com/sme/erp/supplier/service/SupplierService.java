package com.sme.erp.supplier.service;

import com.sme.erp.enums.Status;
import com.sme.erp.supplier.dto.ApReconciliationDTO;
import com.sme.erp.supplier.dto.SupplierDetailDTO;
import com.sme.erp.supplier.dto.SupplierDTO;
import com.sme.erp.supplier.dto.SupplierOptionDTO;
import com.sme.erp.supplier.dto.SupplierPageDTO;
import com.sme.erp.supplier.dto.SupplierLedgerDTO;
import com.sme.erp.supplier.dto.SupplierAgingReportDTO;
import com.sme.erp.supplier.dto.SupplierStatementDTO;

import java.time.LocalDate;
import java.util.List;

public interface SupplierService {
    List<SupplierDTO> getAll(String keyword, Status status);
    SupplierPageDTO searchPage(String keyword, Status status, int page, int size, String sort, String direction);
    List<SupplierOptionDTO> autocomplete(String keyword);
    SupplierDTO getById(Long id);
    SupplierDetailDTO getDetail(Long id);
    SupplierLedgerDTO getLedger(Long id, LocalDate fromDate, LocalDate toDate);
    SupplierStatementDTO getStatement(Long id, LocalDate fromDate, LocalDate toDate);
    SupplierAgingReportDTO getAging(Long supplierId, LocalDate fromDate, LocalDate toDate);
    ApReconciliationDTO getApReconciliation(Long supplierId, LocalDate fromDate, LocalDate toDate);
    SupplierDTO create(SupplierDTO dto);
    SupplierDTO update(Long id, SupplierDTO dto);
    void delete(Long id);
}
