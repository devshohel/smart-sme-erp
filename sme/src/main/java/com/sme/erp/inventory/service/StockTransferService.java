package com.sme.erp.inventory.service;

import com.sme.erp.inventory.dto.StockTransferDTO;
import com.sme.erp.inventory.dto.StockTransferPageDTO;
import com.sme.erp.inventory.enums.StockTransferStatus;

import java.time.LocalDate;
import java.util.List;

public interface StockTransferService {
    List<StockTransferDTO> getAll();
    StockTransferPageDTO search(String keyword, Long fromWarehouseId, Long toWarehouseId, StockTransferStatus status,
                                LocalDate fromDate, LocalDate toDate, int page, int size, String sort, String direction);
    StockTransferDTO getById(Long id);
    StockTransferDTO create(StockTransferDTO dto);
    StockTransferDTO update(Long id, StockTransferDTO dto);
    StockTransferDTO approve(Long id);
    StockTransferDTO send(Long id);
    StockTransferDTO receive(Long id);
    StockTransferDTO cancel(Long id);
}
