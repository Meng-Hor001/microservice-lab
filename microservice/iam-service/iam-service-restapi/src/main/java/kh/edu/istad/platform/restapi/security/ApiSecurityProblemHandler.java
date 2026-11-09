package kh.edu.istad.platform.restapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

@Component
public class ApiSecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    private final BearerTokenAuthenticationEntryPoint authenticationEntryPoint =
            new BearerTokenAuthenticationEntryPoint();

    private final BearerTokenAccessDeniedHandler accessDeniedHandler =
            new BearerTokenAccessDeniedHandler();

    public ApiSecurityProblemHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        authenticationEntryPoint.commence(request, response, exception);

        writeProblem(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "authentication-required",
                "Authentication required",
                "A valid bearer token is required."
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException, ServletException {
        accessDeniedHandler.handle(request, response, exception);

        writeProblem(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "access-denied",
                "Access denied",
                "You do not have permission to perform this operation."
        );
    }

    private void writeProblem(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String type,
            String title,
            String detail
    ) throws IOException {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
