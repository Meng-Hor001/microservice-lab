package kh.edu.istad.platform.iam.application.port.out;

import java.util.List;

public interface ConnectedAppsPort {
    record App(String id, String name, String scopes) {
    }

    List<App> list(String principal);

    void revoke(String principal, String clientId);
}
