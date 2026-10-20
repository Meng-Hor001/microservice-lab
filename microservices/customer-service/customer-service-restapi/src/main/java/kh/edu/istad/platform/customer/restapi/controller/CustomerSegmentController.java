package kh.edu.istad.platform.customer.restapi.controller;


import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.entity.CustomerSegment;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerSegment> create(
            @Valid @RequestBody SegmentRequest request
    ) {
        return segmentCrudUseCase.create(
                request.name(),
                request.description()
        );
    }

    @GetMapping("/{id}")
    public Mono<CustomerSegment> findById(
            @PathVariable String id
    ) {
        return segmentCrudUseCase.findById(id);
    }

    @GetMapping
    public Flux<CustomerSegment> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SegmentName name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return segmentCrudUseCase.search(
                keyword,
                name,
                page,
                size
        );
    }

    @PutMapping("/{id}")
    public Mono<CustomerSegment> update(
            @PathVariable String id,
            @Valid @RequestBody SegmentRequest request
    ) {
        return segmentCrudUseCase.update(
                id,
                request.name(),
                request.description()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable String id
    ) {
        return segmentCrudUseCase.delete(id);
    }
}
