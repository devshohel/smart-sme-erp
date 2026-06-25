package com.sme.erp.auth.repository;

import com.sme.erp.auth.entity.BlacklistedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BlacklistedAccessTokenRepository extends JpaRepository<BlacklistedAccessToken, Long> {
    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    @Modifying
    @Query("delete from BlacklistedAccessToken t where t.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
