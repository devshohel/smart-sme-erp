package com.sme.erp.accounting.service; import com.sme.erp.accounting.entity.YearEndClosing; import java.util.List;
public interface YearEndClosingService { List<YearEndClosing> all(); YearEndClosing prepare(Integer year); YearEndClosing complete(Long id); }
