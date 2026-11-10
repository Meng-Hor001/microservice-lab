package kh.edu.istad.platform.web;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import org.springframework.stereotype.Controller;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@Controller
public class IamWebController {
    private final MessageSource messages;
    private final RegisterUserUseCase register;
    private final LoadUserForAuthenticationUseCase load;
    private final GetUserAuthoritiesUseCase authorities;
    private final UpdateUserProfileUseCase update;
    private final ChangePasswordUseCase password;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlink;

    public IamWebController(MessageSource messages, RegisterUserUseCase register, LoadUserForAuthenticationUseCase load, GetUserAuthoritiesUseCase authorities, UpdateUserProfileUseCase update, ChangePasswordUseCase password, ListUserIdentitiesUseCase identities, UnlinkIdentityUseCase unlink) {
        this.messages = messages;
        this.register = register;
        this.load = load;
        this.authorities = authorities;
        this.update = update;
        this.password = password;
        this.identities = identities;
        this.unlink = unlink;
    }

    AuthUserResult actor(Authentication a) {
        if (a.getPrincipal() instanceof UserDetails u) return load.execute(u.getUsername());
        throw new org.springframework.security.access.AccessDeniedException("Unsupported principal");
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String registration() {
        return "register";
    }

    @PostMapping("/register")
    String registration(@RequestParam Map<String, String> v, Model model, @ModelAttribute("page") Page page) {
        try {
            register.execute(new RegisterUserCommand(v.get("username"), v.get("email"), v.get("password"), v.get("confirmPassword"), v.get("givenName"), v.get("familyName"), v.getOrDefault("locale", "en")));
            return "redirect:/login?registered";
        } catch (IllegalArgumentException | kh.edu.istad.platform.iam.domain.exception.DomainRuleException |
                 kh.edu.istad.platform.iam.application.exception.DuplicateUserException e) {
            return invalid("register", v, model, page);
        }
    }

    @GetMapping("/")
    String index(Authentication a, Model m) {
        var u = actor(a);
        m.addAttribute("user", u);
        m.addAttribute("authorities", authorities.execute(u.uuid()));
        return "index";
    }

    @GetMapping("/profile/edit")
    String edit(Authentication a, Model m) {
        m.addAttribute("user", actor(a));
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    String edit(Authentication a, @RequestParam Map<String, String> v, Model m, @ModelAttribute("page") Page page) {
        var u = actor(a);
        try {
            update.execute(new UpdateUserProfileCommand(u.uuid(), v.get("email"), v.get("givenName"), v.get("familyName"), v.get("middleName"), v.get("nickname"), v.get("gender"), v.getOrDefault("birthdate", "").isBlank() ? null : java.time.LocalDate.parse(v.get("birthdate")), v.get("profileUrl"), v.get("pictureUrl"), v.get("websiteUrl"), v.get("zoneinfo"), v.get("locale"), v.get("phoneNumber"), v.get("addressStreetAddress"), v.get("addressLocality"), v.get("addressRegion"), v.get("addressPostalCode"), v.get("addressCountry")));
            return "redirect:/";
        } catch (IllegalArgumentException | kh.edu.istad.platform.iam.domain.exception.DomainRuleException |
                 kh.edu.istad.platform.iam.application.exception.DuplicateUserException e) {
            m.addAttribute("user", u);
            return invalid("profile-edit", v, m, page);
        }
    }

    @GetMapping("/profile/password")
    String password() {
        return "change-password";
    }

    @PostMapping("/profile/password")
    String password(Authentication a, @RequestParam Map<String, String> v, Model m, @ModelAttribute("page") Page page) {
        try {
            password.execute(new ChangePasswordCommand(actor(a).uuid(), v.get("currentPassword"), v.get("newPassword"), v.get("confirmPassword")));
            return "redirect:/";
        } catch (IllegalArgumentException | kh.edu.istad.platform.iam.domain.exception.DomainRuleException |
                 kh.edu.istad.platform.iam.application.exception.DuplicateUserException e) {
            return invalid("change-password", Map.of(), m, page);
        }
    }

    @GetMapping("/profile/accounts")
    String accounts(Authentication a, Model m) {
        m.addAttribute("identities", identities.execute(actor(a).uuid()));
        return "linked-accounts";
    }

    @PostMapping("/profile/accounts/{provider}/link")
    String link(Authentication a, @PathVariable String provider, HttpServletRequest request) {
        if (!Set.of("google", "github", "facebook", "telegram").contains(provider))
            throw new IllegalArgumentException("Unknown provider");
        var u = actor(a);
        request.getSession().setAttribute("iam.link", Map.of("provider", provider, "uuid", u.uuid().toString(), "username", u.username(), "expires", System.currentTimeMillis() + 300000L));
        return "redirect:/oauth2/authorization/" + provider;
    }

    @PostMapping("/profile/accounts/{id}/unlink")
    String unlink(Authentication a, @PathVariable UUID id, Model m, @ModelAttribute("page") Page page) {
        try {
            unlink.execute(actor(a).uuid(), id);
            return "redirect:/profile/accounts";
        } catch (IllegalArgumentException | kh.edu.istad.platform.iam.domain.exception.DomainRuleException |
                 kh.edu.istad.platform.iam.application.exception.DuplicateUserException e) {
            m.addAttribute("identities", identities.execute(actor(a).uuid()));
            return invalid("linked-accounts", Map.of(), m, page);
        }
    }

    String invalid(String view, Map<String, String> values, Model model, Page page) {
        var safe = new HashMap<>(values);
        safe.keySet().removeIf(k -> k.toLowerCase().contains("password"));
        model.addAttribute("page", new Page(messages, page.locale(), page.csrfName(), page.csrfToken(), safe, "validation.failed"));
        return view;
    }
}
