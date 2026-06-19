package com.sme.erp.purchase.service.impl;

import com.sme.erp.common.exception.ResourceNotFoundException;
import com.sme.erp.purchase.dto.PurchaseReceiveDTO;
import com.sme.erp.purchase.mapper.PurchaseReceiveMapper;
import com.sme.erp.purchase.repository.GoodsReceiveNoteRepository;
import com.sme.erp.purchase.service.PurchaseReceiveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseReceiveServiceImpl implements PurchaseReceiveService {
    private final GoodsReceiveNoteRepository goodsReceiveNoteRepository;
    private final PurchaseReceiveMapper purchaseReceiveMapper;

    public PurchaseReceiveServiceImpl(GoodsReceiveNoteRepository goodsReceiveNoteRepository,
                                      PurchaseReceiveMapper purchaseReceiveMapper) {
        this.goodsReceiveNoteRepository = goodsReceiveNoteRepository;
        this.purchaseReceiveMapper = purchaseReceiveMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseReceiveDTO> getAll() {
        return goodsReceiveNoteRepository.findAll().stream()
                .map(purchaseReceiveMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReceiveDTO getById(Long id) {
        return goodsReceiveNoteRepository.findById(id)
                .map(purchaseReceiveMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Goods receive note not found with id: " + id));
    }
}
