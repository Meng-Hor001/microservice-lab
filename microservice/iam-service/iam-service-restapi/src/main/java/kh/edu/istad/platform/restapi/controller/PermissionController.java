package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.query.PermissionFilterQuery;
import kh.edu.istad.platform.iam.application.usecase.*;
import kh.edu.istad.platform.restapi.dto.request.CreatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.request.UpdatePermissionRequest;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import kh.edu.istad.platform.restapi.dto.response.PermissionResponse;
import kh.edu.istad.platform.restapi.mapper.PermissionApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final CreatePermissionUseCase createPermission;
    private final GetPermissionUseCase getPermission;
    private final ListPermissionUseCase listPermissions;
    private final UpdatePermissionUseCase updatePermission;
    private final DeletePermissionUseCase deletePermission;
    private final ActivePermissionUseCase activatePermission;
    private final DeactivatePermissionUseCase deactivatePermission;
    private final PermissionApiMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<PermissionResponse> create(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        var permission = createPermission.execute(
                mapper.toCommand(request)
        );

        return ResponseEntity
                .created(URI.create("/api/permissions/" + permission.uuid()))
                .body(mapper.toResponse(permission));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:read')")
    public PermissionResponse getByUuid(@PathVariable UUID uuid) {
        return mapper.toResponse(getPermission.execute(uuid));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public PageResponse<PermissionResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String resource,
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

        var query = new PermissionFilterQuery(
                search,
                resource,
                active,
                page,
                size,
                field,
                direction.equalsIgnoreCase("desc")
        );

        return PageResponse.from(
                listPermissions.execute(query),
                mapper::toResponse
        );
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:update')")
    public PermissionResponse update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return mapper.toResponse(
                updatePermission.execute(mapper.toCommand(uuid, request))
        );
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        deletePermission.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/activate")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<Void> activate(@PathVariable UUID uuid) {
        activatePermission.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        deactivatePermission.execute(uuid);

        return ResponseEntity.noContent().build();
    }
}
