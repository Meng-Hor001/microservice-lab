package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.CurrentClientInfo;
import kh.edu.istad.platform.iam.application.usecase.ListAuditLogsUseCase;
import kh.edu.istad.platform.iam.application.usecase.RecordAuditLogUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class AuditLogUseCaseConfig {
    @Bean
    public RecordAuditLogUseCase recordAuditLogUseCase(
            AuditLogRepository auditLogs,
            CurrentActorProvider actors,
            CurrentClientInfo client,
            Clock clock) {
        return new RecordAuditLogUseCase(auditLogs, actors, client, clock);
    }

    @Bean
    public ListAuditLogsUseCase listAuditLogsUseCase(AuditLogRepository auditLog){
        return new ListAuditLogsUseCase(auditLog);
    }

}
