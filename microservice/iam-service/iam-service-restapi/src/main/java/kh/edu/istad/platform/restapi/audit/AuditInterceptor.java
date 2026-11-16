package kh.edu.istad.platform.restapi.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.istad.platform.iam.application.usecase.RecordAuditLogUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import org.slf4j.Logger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);
    private final RecordAuditLogUseCase recordAuditLog;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) {
        if (exception != null || response.getStatus() >= 400 || !(handler instanceof HandlerMethod method)) {
            return;
        }
        Audited audited = method.getMethodAnnotation(Audited.class);
        if (audited == null) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> path = (Map<String, String>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            path = path == null ? Map.of() : path;

            // Target id: {uuid} in the path, or (on create) the last segment of the Location header.
            String targetId = path.containsKey("uuid") ? path.get("uuid") : path.get("clientId");
            if (targetId == null && response.getHeader("Location") != null) {
                String location = response.getHeader("Location");
                targetId = location.substring(location.lastIndexOf('/') + 1);
            }
            // Detail: any other path ids, e.g. roleUuid=…, permissionUuid=…
            String detail = path.entrySet().stream()
                    .filter(e -> !e.getKey().equals("uuid") && !e.getKey().equals("clientId"))
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));

            recordAuditLog.execute(audited.action(), audited.target(), targetId, detail.isEmpty() ? null : detail);
        } catch (RuntimeException e) {
            // Never break the admin's request because the audit write failed.
            log.warn("Could not write audit log for {}: {}", audited.action(), e.getClass().getSimpleName());
        }
    }
}
