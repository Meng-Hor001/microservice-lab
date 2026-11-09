package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.application.query.RoleFilterQuery;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.dto.request.AssignPermissionsToRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.CreateRoleRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdateRoleRequest;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.PermissionAssignmentResponse;
import kh.edu.istad.platform.restapi.dto.response.RoleResponse;
import kh.edu.istad.platform.restapi.dto.response.RoleSummaryResponse;
import kh.edu.istad.platform.restapi.mapper.RoleApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final CreateRoleUseCase createRole;
    private final GetRoleUseCase getRole;
    private final ListRolesUseCase listRoles;
    private final UpdateRoleUseCase updateRole;
    private final DeleteRoleUseCase deleteRole;
    private final ActiveRoleUseCase activateRole;
    private final DeactivateRoleUseCase deactivateRole;
    private final AssignmentPermissionsToRoleUseCase assignPermissions;
    private final RevokePermissionFromRoleUseCase revokePermission;
    private final RoleApiMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<RoleResponse> create(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        var role = createRole.execute(mapper.toCommand(request));

        return ResponseEntity
                .created(URI.create("/api/roles/" + role.uuid()))
                .body(mapper.toResponse(role));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:read')")
    public RoleResponse getByUuid(@PathVariable UUID uuid) {
        return mapper.toResponse(getRole.execute(uuid));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public PageResponse<RoleSummaryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active
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

        var query = new RoleFilterQuery(
                search,
                active,
                page,
                size,
                field,
                direction.equalsIgnoreCase("desc")
        );

        return PageResponse.from(
                listRoles.execute(query),
                mapper::toResponse
        );
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:update')")
    public RoleResponse update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return mapper.toResponse(
                updateRole.execute(mapper.toCommand(uuid, request))
        );
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        deleteRole.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/activate")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        activateRole.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        deactivateRole.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('role:assign')")
    public List<PermissionAssignmentResponse> assignPermissions(
            @PathVariable UUID uuid,
            @Valid @RequestBody AssignPermissionsToRoleRequest request
    ) {
        return assignPermissions.execute(mapper.toCommand(uuid, request))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    @PreAuthorize("hasAuthority('role:assign')")
    public ResponseEntity<Void> revokePermission(
            @PathVariable UUID uuid,
            @PathVariable UUID permissionUuid,
            @RequestParam(required = false)
            @Size(max = 255) String reason
    ) {
        revokePermission.execute(uuid, permissionUuid, reason);

        return ResponseEntity.noContent().build();
    }
}
