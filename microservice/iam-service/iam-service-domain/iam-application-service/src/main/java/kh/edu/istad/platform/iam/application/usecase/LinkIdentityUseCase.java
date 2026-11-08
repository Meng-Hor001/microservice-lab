package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.application.dto.IdentityResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.Identity;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class LinkIdentityUseCase {
    private final UserRepository users;
    private final IdentityRepository identities;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public LinkIdentityUseCase(
            UserRepository users,
            IdentityRepository identities,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.identities = Objects.requireNonNull(identities);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public IdentityResult execute(
            UUID userUuid,
            FederatedIdentityCommand command
    ) {
        Objects.requireNonNull(userUuid, "User UUID is required");
        Objects.requireNonNull(command, "Federated identity is required");

        var user = users.findByUuid(userUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userUuid));

        if (identities.findByProviderAndSubject(
                command.provider(),
                command.providerSubject()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "This provider account is already linked"
            );
        }

        var existing = identities.findByUserUuid(userUuid);
        var now = clock.instant();
        var actor = actorProvider.currentActor();

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

        identity.ensureCanLink(user, existing);

        return IdentityResult.from(identities.save(identity));
    }
}
