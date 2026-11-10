package kh.edu.istad.platform.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.context.MessageSource;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@ControllerAdvice(basePackages = "kh.edu.istad.platform.web")
public class WebPageAdvice {
    private final MessageSource messages;

    public WebPageAdvice(MessageSource messages) {
        this.messages = messages;
    }

    @ModelAttribute("page")
    Page page(HttpServletRequest request, Locale locale) {
        var token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return new Page(messages, locale, token == null ? "_csrf" : token.getParameterName(), token == null ? "" : token.getToken(), Map.of(), request.getParameterMap().containsKey("error") ? "login.failed" : "");
    }
}
