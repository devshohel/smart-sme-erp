package com.sme.erp.inventory.repository;

import com.sme.erp.inventory.entity.StockTransfer;
import com.sme.erp.inventory.enums.StockTransferStatus;
import com.sme.erp.reports.dto.StockTransferRowDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long>, JpaSpecificationExecutor<StockTransfer> {
    StockTransfer findTopByOrderByIdDesc();

    @Query("""
            select new com.sme.erp.reports.dto.StockTransferRowDTO(
                t.transferNo,
                fw.name,
                tw.name,
                t.status,
                t.transferDate,
                count(item.id),
                coalesce(sum(item.quantity), 0)
            )
            from StockTransfer t
            join t.fromWarehouse fw
            join t.toWarehouse tw
            left join t.items item
            where (:fromDate is null or t.transferDate >= :fromDate)
              and (:toDate is null or t.transferDate <= :toDate)
              and (:warehouseId is null or fw.id = :warehouseId or tw.id = :warehouseId)
              and (:status is null or t.status = :status)
              and (:keyword is null or lower(t.transferNo) like lower(concat('%', :keyword, '%'))
                   or lower(fw.name) like lower(concat('%', :keyword, '%'))
                   or lower(tw.name) like lower(concat('%', :keyword, '%')))
            group by t.id, t.transferNo, fw.name, tw.name, t.status, t.transferDate
            order by t.transferDate desc, t.id desc
            """)
    List<StockTransferRowDTO> findTransferReportRows(@Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate,
                                                     @Param("warehouseId") Long warehouseId,
                                                     @Param("status") StockTransferStatus status,
                                                     @Param("keyword") String keyword);
}
