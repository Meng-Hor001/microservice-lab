package kh.edu.istad.platform.business.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.business.application.usecase.GetAllBusinessUseCase;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.restapi.mapper.BusinessWebMapper;
import kh.edu.istad.platform.business.application.dto.RegisterBusinessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import kh.edu.istad.platform.business.application.usecase.RegisterBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.RegisterBusinessRequest;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final RegisterBusinessUseCase registerBusinessUseCase;
    private final BusinessWebMapper businessWebMapper;
    private final GetAllBusinessUseCase getAllBusinessUseCase;



    @GetMapping
    public Flux<Business> getAll(){
        return getAllBusinessUseCase.execute();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RegisterBusinessResult> register(@Valid @RequestBody RegisterBusinessRequest request) {
        return registerBusinessUseCase.execute(businessWebMapper.toCommand(request));
    }
}
