package com.sme.erp.accounting.repository;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountCodeIgnoreCase(String accountCode);
    boolean existsByAccountCodeIgnoreCaseAndIdNot(String accountCode, Long id);
    Optional<Account> findByAccountNameIgnoreCase(String accountName);
    Optional<Account> findByAccountCodeIgnoreCase(String accountCode);

    @Query("select a from Account a where (:type is null or a.accountType = :type) and (:status is null or a.status = :status) order by a.accountCode")
    List<Account> search(@Param("type") AccountType type, @Param("status") Status status);
}
