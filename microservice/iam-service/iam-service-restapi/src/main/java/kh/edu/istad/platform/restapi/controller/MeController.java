package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.dto.request.ChangePasswordRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateUserProfileRequest;
import kh.edu.istad.platform.restapi.dto.response.IdentityResponse;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import kh.edu.istad.platform.restapi.mapper.UserApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MeController {
    private final GetUserUseCase getUser;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final UpdateUserProfileUseCase updateUserProfile;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase listIdentities;
    private final UnlinkIdentityUseCase unlinkIdentity;
    private final UserApiMapper mapper;

    @GetMapping
    public UserResponse getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        var user = getUser.execute(userUuid);
        var authorities = getAuthorities.execute(userUuid);

        return mapper.toResponse(user, authorities);
    }

    @PatchMapping
    public UserResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        var user = updateUserProfile.execute(
                mapper.toCommand(userUuid, request)
        );

        var authorities = getAuthorities.execute(userUuid);

        return mapper.toResponse(user, authorities);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        changePassword.execute(mapper.toCommand(userUuid, request));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/identities")
    public List<IdentityResponse> listIdentities(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        return listIdentities.execute(userUuid)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/identities/{identityUuid}")
    public ResponseEntity<Void> unlinkIdentity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID identityUuid
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        unlinkIdentity.execute(userUuid, identityUuid);

        return ResponseEntity.noContent().build();
    }
}
