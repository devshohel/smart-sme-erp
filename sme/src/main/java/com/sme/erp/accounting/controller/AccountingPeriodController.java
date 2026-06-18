package com.sme.erp.accounting.controller; import com.sme.erp.accounting.entity.AccountingPeriod; import com.sme.erp.accounting.service.AccountingPeriodService; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/v1/accounting/periods") public class AccountingPeriodController {private final AccountingPeriodService service;public AccountingPeriodController(AccountingPeriodService s){service=s;}
 @GetMapping @PreAuthorize("hasAuthority('ACCOUNTING_PERIOD_VIEW')") public List<AccountingPeriod> all(){return service.all();}
 @PostMapping @PreAuthorize("hasAuthority('ACCOUNTING_PERIOD_CLOSE')") public AccountingPeriod create(@RequestBody AccountingPeriod p){return service.create(p);}
 @PostMapping("/{id}/close") @PreAuthorize("hasAuthority('ACCOUNTING_PERIOD_CLOSE')") public AccountingPeriod close(@PathVariable Long id){return service.close(id);}
 @PostMapping("/{id}/reopen") @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('ACCOUNTING_PERIOD_REOPEN')") public AccountingPeriod reopen(@PathVariable Long id){return service.reopen(id);}
}
