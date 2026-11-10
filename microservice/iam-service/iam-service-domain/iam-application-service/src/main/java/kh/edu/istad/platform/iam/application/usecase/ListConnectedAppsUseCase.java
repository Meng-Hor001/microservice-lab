package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort;

public final class ListConnectedAppsUseCase {
    private final ConnectedAppsPort apps;

    public ListConnectedAppsUseCase(ConnectedAppsPort apps) {
        this.apps = java.util.Objects.requireNonNull(apps);
    }

    public java.util.List<ConnectedAppsPort.App> execute(String principal) {
        return apps.list(principal);
    }
}
