package com.sme.erp.sales.pos.service;

import com.sme.erp.sales.pos.dto.PosCompleteRequestDTO;
import com.sme.erp.sales.pos.dto.PosCompleteResponseDTO;

public interface PosService {
    PosCompleteResponseDTO complete(PosCompleteRequestDTO request);
}
