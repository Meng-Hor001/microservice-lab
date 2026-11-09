package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AuthUserResult;
import kh.edu.istad.platform.iam.application.dto.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.application.port.out.TransactionRunner;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.Identity;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class FindOrProvisionFederatedUserUseCase {
    private final UserRepository users;
    private final IdentityRepository identities;
    private final TransactionRunner transaction;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public FindOrProvisionFederatedUserUseCase(
            UserRepository users,
            IdentityRepository identities,
            TransactionRunner transaction,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.identities = Objects.requireNonNull(identities);
        this.transaction = Objects.requireNonNull(transaction);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public AuthUserResult execute(FederatedIdentityCommand command) {
        return transaction.execute(() -> findOrProvision(command));
    }

    private AuthUserResult findOrProvision(FederatedIdentityCommand command) {
        Objects.requireNonNull(command, "Federated identity is required");

        var existing = identities.findByProviderAndSubject(
                command.provider(),
                command.providerSubject()
        );

        if (existing.isPresent()) {
            User user = users.findById(existing.get().getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User", existing.get().getUserId()
                    ));
            return AuthUserResult.from(user);
        }

        if (command.email() == null || command.email().isBlank()) {
            throw new IllegalArgumentException(
                    "Provider must supply an email to provision a user"
            );
        }

        if (users.existsByEmail(command.email())) {
            throw new IllegalArgumentException(
                    "Email already belongs to another account"
            );
        }

        var now = clock.instant();
        var actor = actorProvider.currentActor();
        String[] nameParts = splitName(command.displayName());
        String username = uniqueUsername(command);

        User user = users.save(User.builder()
                .username(username)
                .email(command.email())
                .emailVerified(command.emailVerified())
                .givenName(nameParts[0])
                .familyName(nameParts[1])
                .pictureUrl(command.pictureUrl())
                .locale("en")
                .createdAt(now)
                .createdBy(actor)
                .build());

        Identity identity = Identity.builder()
                .userId(user.getId())
                .provider(command.provider())
                .providerSubject(command.providerSubject())
                .email(command.email())
                .emailVerified(command.emailVerified())
                .displayName(command.displayName())
                .username(command.username())
                .pictureUrl(command.pictureUrl())
                .attributes(command.attributes())
                .linkedAt(now)
                .createdAt(now)
                .createdBy(actor)
                .build();

        identities.save(identity);
        return AuthUserResult.from(user);
    }

    private String[] splitName(String displayName) {
        String value = displayName == null || displayName.isBlank()
                ? "Federated User"
                : displayName.trim();

        int splitAt = value.indexOf(' ');
        if (splitAt < 0) {
            return new String[]{value, "User"};
        }

        return new String[]{
                value.substring(0, splitAt),
                value.substring(splitAt + 1).trim()
        };
    }

    private String uniqueUsername(FederatedIdentityCommand command) {
        String candidate = command.username();

        if (candidate == null || candidate.isBlank()) {
            candidate = command.provider().name().toLowerCase()
                    + "_"
                    + command.providerSubject()
                    .replaceAll("[^A-Za-z0-9]", "");
        }

        candidate = candidate.substring(0, Math.min(candidate.length(), 40));

        if (users.existsByUsername(candidate)) {
            candidate = candidate + "_" +
                    UUID.randomUUID().toString().substring(0, 8);
        }

        return candidate;
    }
}
