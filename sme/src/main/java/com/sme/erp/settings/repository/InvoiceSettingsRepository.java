package com.sme.erp.settings.repository;

import com.sme.erp.settings.entity.InvoiceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSettingsRepository extends JpaRepository<InvoiceSettings, Long> {
}
