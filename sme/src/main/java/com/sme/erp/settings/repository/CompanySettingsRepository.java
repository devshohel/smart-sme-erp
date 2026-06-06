package com.sme.erp.settings.repository;

import com.sme.erp.settings.entity.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
}
