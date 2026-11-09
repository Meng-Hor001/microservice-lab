package kh.edu.istad.platform.restapi.mapper;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;
import kh.edu.istad.platform.restapi.dto.request.*;
import kh.edu.istad.platform.restapi.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserApiMapper {
    RegisterUserCommand toCommand(RegisterUserRequest request);


    UserAuthoritiesResponse toResponse(UserAuthoritiesResult authorities);

    UserSummaryResponse toResponses(UserSummary user);

    IdentityResponse toResponse(IdentityResult identity);

    @Mapping(target = "profile", source = "request")
    CreateUserCommand toCommand(CreateUserRequest request);

    @Mapping(target = "userUuid", source = "uuid")
    ChangePasswordCommand toCommand(
            UUID uuid,
            ChangePasswordRequest request
    );

    @Mapping(target = "addressStreetAddress", source = "address.streetAddress")
    @Mapping(target = "addressLocality", source = "address.locality")
    @Mapping(target = "addressRegion", source = "address.region")
    @Mapping(target = "addressPostalCode", source = "address.postalCode")
    @Mapping(target = "addressCountry", source = "address.country")
    UserProfile toProfile(CreateUserRequest request);


    @Mapping(target = "userUuid", source = "uuid")
    @Mapping(target = "addressStreetAddress", source = "request.address.streetAddress")
    @Mapping(target = "addressLocality", source = "request.address.locality")
    @Mapping(target = "addressRegion", source = "request.address.region")
    @Mapping(target = "addressPostalCode", source = "request.address.postalCode")
    @Mapping(target = "addressCountry", source = "request.address.country")
    UpdateUserProfileCommand toCommand(
            UUID uuid,
            UpdateUserProfileRequest request
    );

    @Mapping(target = "userUuid", source = "uuid")
    ResetUserPasswordCommand toCommand(
            UUID uuid,
            ResetUserPasswordRequest request
    );

    @Mapping(target = "userUuid", source = "uuid")
    AssignRolesToUserCommand toCommand(
            UUID uuid,
            AssignRolesToUserRequest request
    );

    RoleAssignmentResponse toResponse(RoleAssignmentResult assignment);

    @Mapping(target = "userUuid", source = "uuid")
    GrantPermissionToUserCommand toCommand(
            UUID uuid,
            GrantPermissionsToUserRequest request
    );

    PermissionGrantResponse toResponse(PermissionGrantResult grant);

    @Mapping(
            target = "name",
            expression = "java(user.givenName() + \" \" + user.familyName())"
    )
    @Mapping(target = "roles", source = "authorities.roles")
    UserResponse toResponse(
            UserResult user,
            UserAuthoritiesResult authorities
    );
}
