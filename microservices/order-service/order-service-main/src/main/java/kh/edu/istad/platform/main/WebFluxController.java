package kh.edu.istad.platform.main;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class WebFluxController {
    @GetMapping("/flux")
    public Flux<String> getFluxData(){
        return Flux.just("A","B","C","D","E","F").log().delayElements(Duration.ofSeconds(1));
    }
}
