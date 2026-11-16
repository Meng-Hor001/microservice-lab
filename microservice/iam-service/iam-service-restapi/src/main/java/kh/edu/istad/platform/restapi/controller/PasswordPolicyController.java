package kh.edu.istad.platform.restapi.controller;

import kh.edu.istad.platform.iam.application.dto.PasswordPolicyResult;
import kh.edu.istad.platform.iam.application.support.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public: sign-up and reset-by-link pages show the live rules before anyone is signed in. */
@RestController
@RequestMapping("/api/auth/password-policy")
@RequiredArgsConstructor
public class PasswordPolicyController {
    private final PasswordPolicy passwordPolicy;

    @GetMapping
    public PasswordPolicyResult get() {
        return passwordPolicy.current();
    }
}
