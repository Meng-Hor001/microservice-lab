package kh.edu.istad.platform.infrastructure.authorization;

import kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ConnectedAppsAdapter implements ConnectedAppsPort {
    private final JdbcOperations jdbc;

    public ConnectedAppsAdapter(JdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    public List<App> list(String principal) {
        return jdbc.query("SELECT c.id,c.client_name,a.authorities FROM oauth2_authorization_consent a JOIN oauth2_registered_client c ON c.id=a.registered_client_id WHERE a.principal_name=?", (rs, n) -> new App(rs.getString(1), rs.getString(2), rs.getString(3)), principal);
    }

    @Transactional
    public void revoke(String principal, String clientId) {
        jdbc.update("DELETE FROM oauth2_authorization WHERE principal_name=? AND registered_client_id=?", principal, clientId);
        jdbc.update("DELETE FROM oauth2_authorization_consent WHERE principal_name=? AND registered_client_id=?", principal, clientId);
    }
}
