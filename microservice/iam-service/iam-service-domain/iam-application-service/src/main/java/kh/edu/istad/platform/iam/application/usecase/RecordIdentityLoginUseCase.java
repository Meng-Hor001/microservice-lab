package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;

import java.time.Clock;
import java.util.UUID;

public final class RecordIdentityLoginUseCase {
    private final IdentityRepository identities;
    private final CurrentActorProvider actor;
    private final Clock clock;

    public RecordIdentityLoginUseCase(
            IdentityRepository identities,
            CurrentActorProvider actor,
            Clock clock
    ) {
        this.identities = identities;
        this.actor = actor;
        this.clock = clock;
    }

    public void execute(UUID userUuid, IdentityProvider provider) {
        var identity = identities.findByUserUuid(userUuid).stream()
                .filter(candidate -> candidate.getProvider() == provider)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Identity not found"));

        identity.recordLogin(actor.currentActor(), clock.instant());
        identities.save(identity);
    }
}
