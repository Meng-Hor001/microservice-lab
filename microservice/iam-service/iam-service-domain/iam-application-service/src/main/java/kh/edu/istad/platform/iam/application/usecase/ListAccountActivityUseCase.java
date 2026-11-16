package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AccountActivityResult;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ListAccountActivityUseCase {
    private final AccountSecurityRepository accounts;

    public ListAccountActivityUseCase(AccountSecurityRepository accounts) {
        this.accounts = Objects.requireNonNull(accounts);
    }

    public List<AccountActivityResult> execute(UUID userId) {
        return accounts.activities(userId);
    }
}
