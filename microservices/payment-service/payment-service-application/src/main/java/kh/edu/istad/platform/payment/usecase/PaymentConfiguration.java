package kh.edu.istad.platform.payment.usecase;

import kh.edu.istad.platform.payment.domain.service.PaymentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PaymentConfiguration {

    @Bean
    Clock paymentClock(){
        return Clock.systemUTC();
    }

    @Bean
    PaymentDomainService paymentDomainService() {
        return new PaymentDomainService();
    }

    @Bean
    ProcessPaymentUseCase processPaymentUseCase(
            PaymentDomainService paymentDomainService,
            Clock paymentClock) {
        return new ProcessPaymentUseCase(paymentDomainService, paymentClock);
    }
}
