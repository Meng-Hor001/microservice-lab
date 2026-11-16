package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.SystemSettingsRepository;
import kh.edu.istad.platform.iam.application.usecase.ManageSystemSettingsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class SystemSettingsUseCaseConfig {
    @Bean
    public ManageSystemSettingsUseCase manageSystemSettingsUseCase(
            SystemSettingsRepository settings, CurrentActorProvider actors, Clock clock) {
        return new ManageSystemSettingsUseCase(settings, actors, clock);
    }
}
