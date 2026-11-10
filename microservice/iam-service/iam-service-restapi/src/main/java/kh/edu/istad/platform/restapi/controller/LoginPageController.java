package kh.edu.istad.platform.restapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

public class LoginPageController {
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String login(HttpServletRequest request) {
        var csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String hidden = csrf == null ? "" : "<input type=\"hidden\" name=\""
                + HtmlUtils.htmlEscape(csrf.getParameterName()) + "\" value=\""
                + HtmlUtils.htmlEscape(csrf.getToken()) + "\">";
        String error = request.getParameterMap().containsKey("error")
                ? "<p role=\"alert\">Sign-in failed. Please try again.</p>" : "";
        String base = HtmlUtils.htmlEscape(request.getContextPath());
        return """
                <!doctype html><html lang="en"><head>
                <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Sign in</title></head><body><main><h1>Sign in</h1>
                %s
                <form method="post" action="%s/login">
                %s
                <p><label>Username or email <input name="username" autocomplete="username" required></label></p>
                <p><label>Password <input type="password" name="password" autocomplete="current-password" required></label></p>
                <button type="submit">Sign in</button></form>
                <p><a href="%s/oauth2/authorization/google">Continue with Google</a></p>
                <p><a href="%s/oauth2/authorization/github">Continue with GitHub</a></p>
                <p><a href="%s/oauth2/authorization/facebook">Continue with Facebook</a></p>
                </main></body></html>
                """.formatted(error, base, hidden, base, base, base);
    }
}
