package kh.edu.istad.platform.web;

import org.springframework.context.MessageSource;

import java.util.*;

public record Page(MessageSource messages, Locale locale, String csrfName, String csrfToken, Map<String, String> values,
                   String error) {
    public String t(String key) {
        return messages.getMessage(key, null, locale);
    }

    public String v(String key) {
        return values.getOrDefault(key, "");
    }
}
