package com.sme.erp.accounting.service;

import com.sme.erp.accounting.dto.JournalEntryDTO;
import com.sme.erp.accounting.enums.JournalStatus;

import java.util.List;

public interface JournalEntryService {
    List<JournalEntryDTO> getAll(JournalStatus status);
    JournalEntryDTO getById(Long id);
    JournalEntryDTO create(JournalEntryDTO dto);
    JournalEntryDTO post(Long id);
    JournalEntryDTO cancel(Long id);
}
