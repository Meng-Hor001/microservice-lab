package kh.edu.istad.platform.webflux.config;

import kh.edu.istad.platform.webflux.exception.DomainExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public DomainExceptionHandler domainExceptionHandler() {
        return new DomainExceptionHandler();
    }
}
