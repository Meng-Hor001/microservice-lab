package kh.edu.istad.platform.customer.restapi.controller;


import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.restapi.dto.SegmentResponse;
import kh.edu.istad.platform.customer.restapi.mapper.SegmentWebMapper;
import kh.edu.istad.platform.customer.domain.usecase.SegmentCrudUseCase;
import kh.edu.istad.platform.customer.domain.valueobject.SegmentName;
import kh.edu.istad.platform.customer.restapi.dto.SegmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer-segments")
public class CustomerSegmentController {

    private final SegmentCrudUseCase segmentCrudUseCase;
    private final SegmentWebMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SegmentResponse> create(
            @Valid @RequestBody SegmentRequest request
    ) {
        return segmentCrudUseCase.create(
                request.name(),
                request.description()
        ).map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<SegmentResponse> findById(
            @PathVariable("id") String id
    ) {
        return segmentCrudUseCase.findById(id).map(mapper::toResponse);
    }

    @GetMapping
    public Flux<SegmentResponse> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "name", required = false) SegmentName name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return segmentCrudUseCase.search(keyword, name, page, size)
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    public Mono<SegmentResponse> update(
            @PathVariable("id") String id,
            @Valid @RequestBody SegmentRequest request
    ) {
        return segmentCrudUseCase.update(
                id,
                request.name(),
                request.description()
        ).map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable("id") String id
    ) {
        return segmentCrudUseCase.delete(id);
    }
}
