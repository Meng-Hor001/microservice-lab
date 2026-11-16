package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.SystemSettingsResult;
import kh.edu.istad.platform.iam.application.dto.UpdateSystemSettingsCommand;

import java.time.Instant;

public interface SystemSettingsRepository {
    SystemSettingsResult get();

    SystemSettingsResult update(UpdateSystemSettingsCommand command, String actor, Instant now);
}
