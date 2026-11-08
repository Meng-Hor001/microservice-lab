package kh.edu.istad.platform.customer.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;

@Configuration
@Profile("local")
public class LocalGrpcSecurityConfig {
    @Bean
    @GlobalServerInterceptor
    AuthenticationProcessInterceptor permitLocalGrpcCalls(GrpcSecurity grpc) throws Exception {
        return grpc
                .authorizeRequests(requests -> requests.allRequests().permitAll())
                .build();
    }
}
