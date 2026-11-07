package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.application.dto.UserResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;

import java.time.Clock;
import java.util.Objects;

public final class UpdateUserProfileUseCase {
    private final UserRepository users;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public UpdateUserProfileUseCase(
            UserRepository users,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public UserResult execute(UpdateUserProfileCommand command) {
        Objects.requireNonNull(command, "Command is required");
        User user = users.findByUuid(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()));

        UserProfile current = new UserProfile(
                user.getEmail(), user.getGivenName(), user.getFamilyName(),
                user.getMiddleName(), user.getNickname(), user.getGender(),
                user.getBirthdate(), user.getProfileUrl(), user.getPictureUrl(),
                user.getWebsiteUrl(), user.getZoneinfo(), user.getLocale(),
                user.getPhoneNumber(), user.getAddressStreetAddress(),
                user.getAddressLocality(), user.getAddressRegion(),
                user.getAddressPostalCode(), user.getAddressCountry()
        );
        UserProfile updated = new UserProfile(
                value(command.email(), current.email()),
                value(command.givenName(), current.givenName()),
                value(command.familyName(), current.familyName()),
                value(command.middleName(), current.middleName()),
                value(command.nickname(), current.nickname()),
                value(command.gender(), current.gender()),
                value(command.birthdate(), current.birthdate()),
                value(command.profileUrl(), current.profileUrl()),
                value(command.pictureUrl(), current.pictureUrl()),
                value(command.websiteUrl(), current.websiteUrl()),
                value(command.zoneinfo(), current.zoneinfo()),
                value(command.locale(), current.locale()),
                value(command.phoneNumber(), current.phoneNumber()),
                value(command.addressStreetAddress(), current.addressStreetAddress()),
                value(command.addressLocality(), current.addressLocality()),
                value(command.addressRegion(), current.addressRegion()),
                value(command.addressPostalCode(), current.addressPostalCode()),
                value(command.addressCountry(), current.addressCountry())
        );

        user.updateProfile(updated, actorProvider.currentActor(), clock.instant());
        return UserResult.from(users.save(user));
    }

    private static <T> T value(T requested, T current) {
        return requested == null ? current : requested;
    }
}
