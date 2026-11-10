package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort;

public final class RevokeConnectedAppUseCase {
    private final ConnectedAppsPort apps;

    public RevokeConnectedAppUseCase(ConnectedAppsPort apps) {
        this.apps = java.util.Objects.requireNonNull(apps);
    }

    public void execute(String principal, String clientId) {
        apps.revoke(principal, clientId);
    }
}
