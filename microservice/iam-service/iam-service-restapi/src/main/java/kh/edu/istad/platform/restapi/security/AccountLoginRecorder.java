package kh.edu.istad.platform.restapi.security;

import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentClientInfo;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class AccountLoginRecorder {
    private final UserRepository users;
    private final AccountSecurityRepository accounts;
    private final CurrentClientInfo client;
    private final Clock clock;

    public AccountLoginRecorder(UserRepository users, AccountSecurityRepository accounts,
                                CurrentClientInfo client, Clock clock) {
        this.users = users;
        this.accounts = accounts;
        this.client = client;
        this.clock = clock;
    }

    public void record(String username, boolean success, String method) {
        if (username == null || username.isBlank()) {
            return;
        }
        try {
            users.findByUsernameOrEmail(username.trim()).ifPresent(user ->
                    accounts.recordLogin(user.getUuid(), success, method, client.current(), clock.instant()));
        } catch (RuntimeException exception) {
            LoggerFactory.getLogger(AccountLoginRecorder.class)
                    .warn("Could not persist account login history: {}", exception.getClass().getSimpleName());
        }
    }
}
