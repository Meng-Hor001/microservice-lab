package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.dto.OAuthClientCredentialsResult;
import kh.edu.istad.platform.iam.application.dto.OAuthClientResult;
import kh.edu.istad.platform.iam.application.usecase.ManageOAuthClientsUseCase;
import kh.edu.istad.platform.restapi.audit.Audited;
import kh.edu.istad.platform.restapi.dto.request.OAuthClientSearchRequest;
import kh.edu.istad.platform.restapi.dto.request.SaveOAuthClientRequest;
import kh.edu.istad.platform.restapi.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** Admin management of OAuth2/OIDC clients for the dashboard. */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class OAuthClientController {
    private final ManageOAuthClientsUseCase clients;

    @GetMapping
    @PreAuthorize("hasAuthority('client:read')")
    public PageResponse<OAuthClientResult> list(@Valid OAuthClientSearchRequest request) {
        return PageResponse.from(
                clients.list(request.search(), request.pageOrDefault(), request.sizeOrDefault()),
                client -> client);
    }

    @GetMapping("/{clientId}")
    @PreAuthorize("hasAuthority('client:read')")
    public OAuthClientResult get(@PathVariable String clientId) {
        return clients.get(clientId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('client:create')")
    @Audited(action = "CLIENT_CREATED", target = "CLIENT")
    public ResponseEntity<OAuthClientCredentialsResult> create(@Valid @RequestBody SaveOAuthClientRequest request) {
        var created = clients.create(request.toCommand());
        return ResponseEntity.created(URI.create("/api/clients/" + created.client().clientId())).body(created);
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasAuthority('client:update')")
    @Audited(action = "CLIENT_UPDATED", target = "CLIENT")
    public OAuthClientResult update(@PathVariable String clientId,
                                    @Valid @RequestBody SaveOAuthClientRequest request) {
        return clients.update(clientId, request.toCommand());
    }

    @PostMapping("/{clientId}/secret")
    @PreAuthorize("hasAuthority('client:update')")
    @Audited(action = "CLIENT_SECRET_ROTATED", target = "CLIENT")
    public OAuthClientCredentialsResult rotateSecret(@PathVariable String clientId) {
        return clients.rotateSecret(clientId);
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasAuthority('client:delete')")
    @Audited(action = "CLIENT_DELETED", target = "CLIENT")
    public ResponseEntity<Void> delete(@PathVariable String clientId) {
        clients.delete(clientId);
        return ResponseEntity.noContent().build();
    }
}
