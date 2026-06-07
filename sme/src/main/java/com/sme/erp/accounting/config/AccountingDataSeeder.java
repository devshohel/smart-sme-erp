package com.sme.erp.accounting.config;

import com.sme.erp.accounting.entity.Account;
import com.sme.erp.accounting.enums.AccountType;
import com.sme.erp.accounting.repository.AccountRepository;
import com.sme.erp.enums.Status;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AccountingDataSeeder implements CommandLineRunner {
    private final AccountRepository accountRepository;

    public AccountingDataSeeder(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<SeedAccount> accounts = List.of(
                new SeedAccount("1000", "Cash", AccountType.ASSET),
                new SeedAccount("1010", "Bank", AccountType.ASSET),
                new SeedAccount("1020", "Accounts Receivable", AccountType.ASSET),
                new SeedAccount("2000", "Accounts Payable", AccountType.LIABILITY),
                new SeedAccount("3000", "Owner Equity", AccountType.EQUITY),
                new SeedAccount("4000", "Sales Income", AccountType.INCOME),
                new SeedAccount("5000", "Purchase Cost", AccountType.EXPENSE),
                new SeedAccount("5100", "Operating Expense", AccountType.EXPENSE));
        accounts.forEach(this::seed);
    }

    private void seed(SeedAccount seed) {
        accountRepository.findByAccountCodeIgnoreCase(seed.code()).orElseGet(() -> {
            Account account = new Account();
            account.setAccountCode(seed.code());
            account.setAccountName(seed.name());
            account.setAccountType(seed.type());
            account.setStatus(Status.ACTIVE);
            return accountRepository.save(account);
        });
    }

    private record SeedAccount(String code, String name, AccountType type) {}
}
