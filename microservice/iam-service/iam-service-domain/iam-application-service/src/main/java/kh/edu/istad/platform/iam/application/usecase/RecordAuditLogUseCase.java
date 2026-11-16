package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.RecordAuditLogCommand;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.CurrentClientInfo;

import java.time.Clock;
import java.util.Objects;

public final class RecordAuditLogUseCase {
    private final AuditLogRepository auditLogs;
    private final CurrentActorProvider actorProvider;
    private final CurrentClientInfo client;
    private final Clock clock;

    public RecordAuditLogUseCase(AuditLogRepository auditLogs, CurrentActorProvider actorProvider,
                                 CurrentClientInfo client, Clock clock) {
        this.auditLogs = Objects.requireNonNull(auditLogs);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.client = Objects.requireNonNull(client);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(String action, String targetType, String targetId, String detail) {
        execute(actorProvider.currentActor(), action, targetType, targetId, detail);
    }

    public void execute(String actor, String action, String targetType, String targetId, String detail) {
        auditLogs.save(new RecordAuditLogCommand(
                actor, action, targetType, targetId, detail, client.current().ip(), clock.instant()));
    }
}
