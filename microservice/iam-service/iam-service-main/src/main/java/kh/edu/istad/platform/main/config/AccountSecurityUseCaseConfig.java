package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class AccountSecurityUseCaseConfig {
    @Bean
    public CurrentClientInfo currentClientInfo() {
        return kh.edu.istad.platform.infrastructure.account.RequestClientInfo::current;
    }

    @Bean
    public ListAccountSessionsUseCase listAccountSessionsUseCase(AccountSecurityRepository accounts, Clock clock) {
        return new ListAccountSessionsUseCase(accounts, clock);
    }

    @Bean
    public RevokeAccountSessionsUseCase revokeAccountSessionsUseCase(AccountSecurityRepository accounts, Clock clock) {
        return new RevokeAccountSessionsUseCase(accounts, clock);
    }

    @Bean
    public ListLoginHistoryUseCase listLoginHistoryUseCase(AccountSecurityRepository accounts, Clock clock) {
        return new ListLoginHistoryUseCase(accounts, clock);
    }

    @Bean
    public ListSuspiciousLoginsUseCase listSuspiciousLoginsUseCase(AccountSecurityRepository accounts, Clock clock) {
        return new ListSuspiciousLoginsUseCase(accounts, clock);
    }

    @Bean
    public ListAccountActivityUseCase listAccountActivityUseCase(AccountSecurityRepository accounts) {
        return new ListAccountActivityUseCase(accounts);
    }

    @Bean
    public ManageDeactivationUseCase manageDeactivationUseCase(AccountSecurityRepository accounts, Clock clock) {
        return new ManageDeactivationUseCase(accounts, clock);
    }

    @Bean
    public RequestPasswordResetUseCase requestPasswordResetUseCase(
            UserRepository users, AccountSecurityRepository accounts, PasswordResetDelivery delivery,
            TransactionRunner transactions, Clock clock
    ) {
        return new RequestPasswordResetUseCase(users, accounts, delivery, transactions, clock);
    }

    @Bean
    public ConfirmPasswordResetUseCase confirmPasswordResetUseCase(
            AccountSecurityRepository accounts, PasswordHasher hasher, Clock clock
    ) {
        return new ConfirmPasswordResetUseCase(accounts, hasher, clock);
    }
}
