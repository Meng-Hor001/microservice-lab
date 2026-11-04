package kh.edu.istad.platform.order.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@EnableAutoConfiguration
@SpringBootConfiguration
@EnableR2dbcRepositories(
        basePackages = "kh.edu.istad.platform.order.persistence.repository"
)
public class PersistenceTestConfig {
}
