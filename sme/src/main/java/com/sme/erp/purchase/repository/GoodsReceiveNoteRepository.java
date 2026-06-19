package com.sme.erp.purchase.repository;

import com.sme.erp.purchase.entity.GoodsReceiveNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoodsReceiveNoteRepository extends JpaRepository<GoodsReceiveNote, Long> {
    Optional<GoodsReceiveNote> findTopByOrderByIdDesc();
    boolean existsByGrnNo(String grnNo);
    List<GoodsReceiveNote> findByPurchaseOrderIdOrderByReceiveDateDescIdDesc(Long purchaseOrderId);
}
