package kh.edu.istad.platform.main;
import kh.edu.istad.platform.persistence.entity.PermissionJpaEntity;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import kh.edu.istad.platform.persistence.repository.PermissionJpaRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "kh.edu.istad.platform")
@EntityScan(basePackageClasses = PermissionJpaEntity.class)
@EnableJpaRepositories(basePackageClasses = PermissionJpaRepository.class)
public class IamApplicationService {
    public static void main(String[] args) {
        SpringApplication.run(IamApplicationService.class, args);
    }
}
