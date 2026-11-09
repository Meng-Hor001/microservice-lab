package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.application.query.UserFilterQuery;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.dto.request.AssignRolesToUserRequest;
import kh.edu.istad.platform.restapi.dto.request.GrantPermissionsToUserRequest;
import kh.edu.istad.platform.restapi.dto.request.ResetUserPasswordRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateUserProfileRequest;
import kh.edu.istad.platform.restapi.dto.response.*;
import kh.edu.istad.platform.restapi.mapper.UserApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUserUseCase getUser;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final ListUserUseCase listUser;
    private final CreateUserUseCase createUser;
    private final DeleteUseUseCase deleteUser;
    private final AssignRolesToUserUseCase assignRoles;
    private final RevokedRoleFromUserUseCase revokeRole;
    private final EnableUserUseCase enableUser;
    private final DisableUserUseCase disableUser;
    private final LockUserUseCase lockUser;
    private final UnlockUserUseCase unlockUser;
    private final ListUserIdentitiesUseCase listIdentities;
    private final UnlinkIdentityUseCase unlinkIdentity;
    private final GrantPermissionToUserUseCase grantPermissions;
    private final RevokePermissionFromUserUseCase revokePermission;
    private final ResetUserPasswordUseCase resetUserPassword;
    private final UpdateUserProfileUseCase updateUserProfile;
    private final UserApiMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public PageResponse<UserSummaryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled
    ) {
        String[] sorting = sort.split(",", -1);

        if (sorting.length != 2) {
            throw new IllegalArgumentException(
                    "Sort must use field,asc or field,desc"
            );
        }

        String field = sorting[0].trim();
        String direction = sorting[1].trim();

        if (field.isEmpty()
                || (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException(
                    "Sort must use field,asc or field,desc"
            );
        }

        var query = new UserFilterQuery(
                search,
                enabled,
                page,
                size,
                field,
                direction.equalsIgnoreCase("desc")
        );

        var result = listUser.execute(query);

        return PageResponse.from(result, mapper::toResponses);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:read')")
    public UserResponse getByUuid(
            @PathVariable UUID uuid
            ){
        var user = getUser.execute(uuid);
        var authorities = getAuthorities.execute(uuid);

        return mapper.toResponse(user, authorities);
    }


    @GetMapping("/{uuid}/authorities")
    @PreAuthorize("hasAuthority('user:read')")
    public UserAuthoritiesResponse getAuthorities(
            @PathVariable UUID uuid
    ) {
        return mapper.toResponse(getAuthorities.execute(uuid));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:update')")
    public UserResponse updateProfile(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        var user = updateUserProfile.execute(
                mapper.toCommand(uuid, request)
        );

        var authorities = getAuthorities.execute(user.uuid());

        return mapper.toResponse(user, authorities);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        deleteUser.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/enable")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> enable(@PathVariable UUID uuid) {
        enableUser.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/disable")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> disable(@PathVariable UUID uuid) {
        disableUser.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/lock")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> lock(@PathVariable UUID uuid) {
        lockUser.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/unlock")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> unlock(@PathVariable UUID uuid) {
        unlockUser.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID uuid,
            @Valid @RequestBody ResetUserPasswordRequest request
    ) {
        resetUserPassword.execute(mapper.toCommand(uuid, request));

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/roles")
    @PreAuthorize("hasAuthority('user:assign')")
    public List<RoleAssignmentResponse> assignRoles(
            @PathVariable UUID uuid,
            @Valid @RequestBody AssignRolesToUserRequest request
    ) {
        return assignRoles.execute(mapper.toCommand(uuid, request))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/roles/{roleUuid}")
    @PreAuthorize("hasAuthority('user:assign')")
    public ResponseEntity<Void> revokeRole(
            @PathVariable UUID uuid,
            @PathVariable UUID roleUuid,
            @RequestParam(required = false)
            @Size(max = 255) String reason
    ) {
        revokeRole.execute(uuid, roleUuid, reason);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('user:assign')")
    public List<PermissionGrantResponse> grantPermissions(
            @PathVariable UUID uuid,
            @Valid @RequestBody GrantPermissionsToUserRequest request
    ) {
        return grantPermissions.execute(mapper.toCommand(uuid, request))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    @PreAuthorize("hasAuthority('user:assign')")
    public ResponseEntity<Void> revokePermission(
            @PathVariable UUID uuid,
            @PathVariable UUID permissionUuid,
            @RequestParam(required = false)
            @Size(max = 255) String reason
    ) {
        revokePermission.execute(uuid, permissionUuid, reason);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{uuid}/identities")
    @PreAuthorize("hasAuthority('user:read')")
    public List<IdentityResponse> listIdentities(
            @PathVariable UUID uuid
    ) {
        return listIdentities.execute(uuid)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/identities/{identityUuid}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> unlinkIdentity(
            @PathVariable UUID uuid,
            @PathVariable UUID identityUuid
    ) {
        unlinkIdentity.execute(uuid, identityUuid);

        return ResponseEntity.noContent().build();
    }

}
