package com.sme.erp.settings.repository;

import com.sme.erp.settings.entity.TaxSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxSettingsRepository extends JpaRepository<TaxSettings, Long> {
}
