package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.UserAvatarRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.usecase.DeleteAvatarUseCase;
import kh.edu.istad.platform.iam.application.usecase.GetAvatarUseCase;
import kh.edu.istad.platform.iam.application.usecase.UploadAvatarUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class AvatarUseCaseConfig {
    @Bean
    public UploadAvatarUseCase uploadAvatarUseCase(UserRepository users, UserAvatarRepository avatars, Clock clock) {
        return new UploadAvatarUseCase(users, avatars, clock);
    }

    @Bean
    public GetAvatarUseCase getAvatarUseCase(UserAvatarRepository avatars) {
        return new GetAvatarUseCase(avatars);
    }

    @Bean
    public DeleteAvatarUseCase deleteAvatarUseCase(UserRepository users, UserAvatarRepository avatars) {
        return new DeleteAvatarUseCase(users, avatars);
    }
}
