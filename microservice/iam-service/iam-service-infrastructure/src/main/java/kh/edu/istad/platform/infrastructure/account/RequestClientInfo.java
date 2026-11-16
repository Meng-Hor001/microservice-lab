package kh.edu.istad.platform.infrastructure.account;

import kh.edu.istad.platform.iam.application.dto.ClientInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

public final class RequestClientInfo {
    private RequestClientInfo() {
    }

    public static ClientInfo current() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return ClientInfo.unknown();
        }
        var request = attributes.getRequest();
        String raw = request.getHeader("User-Agent");
        String agent = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
        String type = agent.contains("ipad") || agent.contains("tablet") ? "tablet"
                : agent.contains("mobile") ? "mobile" : "desktop";
        String os = agent.contains("android") ? "Android" : agent.contains("iphone") || agent.contains("ipad") ? "iOS"
                : agent.contains("windows") ? "Windows" : agent.contains("mac") ? "macOS"
                : agent.contains("linux") ? "Linux" : "Unknown";
        String browser = agent.contains("edg/") ? "Edge" : agent.contains("firefox/") ? "Firefox"
                : agent.contains("chrome/") ? "Chrome" : agent.contains("safari/") ? "Safari" : "Unknown";
        // Do not trust arbitrary X-Forwarded-For headers without trusted proxy configuration.
        return new ClientInfo(type, os + " device", browser, os, request.getRemoteAddr());
    }
}
