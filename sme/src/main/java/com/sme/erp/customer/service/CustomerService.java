package com.sme.erp.customer.service;

import com.sme.erp.customer.dto.CustomerAgingReportDTO;
import com.sme.erp.customer.dto.CustomerDTO;
import com.sme.erp.customer.dto.CustomerDetailDTO;
import com.sme.erp.customer.dto.CustomerLedgerDTO;
import com.sme.erp.customer.dto.CustomerOptionDTO;
import com.sme.erp.customer.dto.CustomerPageDTO;
import com.sme.erp.enums.Status;

import java.time.LocalDate;
import java.util.List;

public interface CustomerService {
    List<CustomerDTO> getAll(String keyword, Status status);
    CustomerPageDTO searchPage(String keyword, Status status, int page, int size, String sort, String direction);
    List<CustomerOptionDTO> autocomplete(String keyword);
    CustomerDetailDTO getDetail(Long id);
    CustomerLedgerDTO getLedger(Long id, LocalDate fromDate, LocalDate toDate);
    CustomerAgingReportDTO getAging(Long customerId, LocalDate fromDate, LocalDate toDate);
    CustomerDTO getById(Long id);
    List<CustomerDTO> getDeleted();
    CustomerDTO create(CustomerDTO dto);
    CustomerDTO update(Long id, CustomerDTO dto);
    void delete(Long id);
    CustomerDTO restore(Long id);
}
