package kh.edu.istad.platform.business.restapi.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.business.application.dto.*;
import kh.edu.istad.platform.business.application.usecase.RegisterBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.RegisterBusinessRequest;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {
    private final RegisterBusinessUseCase useCase;
    public BusinessController(RegisterBusinessUseCase useCase) { this.useCase = useCase; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegisterBusinessResult> register(@Valid @RequestBody RegisterBusinessRequest request) {
        return useCase.execute(new RegisterBusinessCommand(request.legalName(), request.displayName(),
                request.email(), request.phoneNumber()));
    }
}
