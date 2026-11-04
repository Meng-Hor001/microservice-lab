package kh.edu.istad.platform.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@EnableConfigurationProperties
@EnableR2dbcRepositories(basePackages = "kh.edu.istad.platform.order.persistence")
@SpringBootApplication
public class OrderApplicationService {

    static void main(String[] args) {
        SpringApplication.run(OrderApplicationService.class, args);
    }

}
