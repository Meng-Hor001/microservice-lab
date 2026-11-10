package kh.edu.istad.platform.web;

import kh.edu.istad.platform.iam.application.usecase.ListConnectedAppsUseCase;
import kh.edu.istad.platform.iam.application.usecase.RevokeConnectedAppUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

@Controller
public class AuthorizationPagesController {
    private final ListConnectedAppsUseCase apps;
    private final RevokeConnectedAppUseCase revoke;
    private final RegisteredClientRepository clients;

    public AuthorizationPagesController(ListConnectedAppsUseCase apps, RevokeConnectedAppUseCase revoke, RegisteredClientRepository clients) {
        this.apps = apps;
        this.revoke = revoke;
        this.clients = clients;
    }

    @GetMapping("/profile/apps")
    String apps(Authentication a, Model m) {
        m.addAttribute("apps", apps.execute(a.getName()));
        return "connected-apps";
    }

    @PostMapping("/profile/apps/{id}/revoke")
    String revoke(Authentication a, @PathVariable String id) {
        revoke.execute(a.getName(), id);
        return "redirect:/profile/apps";
    }

    @GetMapping("/oauth2/consent")
    String consent(@RequestParam("client_id") String clientId, @RequestParam String scope, @RequestParam String state, Model m) {
        var c = clients.findByClientId(clientId);
        if (c == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var scopes = Arrays.stream(scope.split(" +")).filter(s -> !s.equals("openid")).toList();
        if (!c.getScopes().containsAll(scopes)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        m.addAttribute("clientName", c.getClientName());
        m.addAttribute("clientId", clientId);
        m.addAttribute("state", state);
        m.addAttribute("scopes", scopes);
        return "consent";
    }
}
