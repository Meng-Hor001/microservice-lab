package kh.edu.istad.platform.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.application.usecase.RegisterUserUseCase;
import kh.edu.istad.platform.restapi.dto.request.RegisterUserRequest;
import kh.edu.istad.platform.restapi.dto.response.UserResponse;
import kh.edu.istad.platform.restapi.mapper.UserApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;
    private final UserApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request
            ) {
        var result = registerUserUseCase.execute(mapper.toCommand(request));
        var authorities = getUserAuthoritiesUseCase.execute(result.uuid());
        var response = mapper.toResponse(result, authorities);
        return ResponseEntity.created(
                URI.create("/api/users" + result.uuid()))
                .body(response);
    }
}
