package com.sme.erp.accounting.service; import com.sme.erp.accounting.entity.AccountingPeriod; import java.time.LocalDate; import java.util.List;
public interface AccountingPeriodService { List<AccountingPeriod> all(); AccountingPeriod create(AccountingPeriod p); AccountingPeriod close(Long id); AccountingPeriod reopen(Long id); void assertOpen(LocalDate date); }
