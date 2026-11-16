package kh.edu.istad.platform.iam.application.port.out;

import kh.edu.istad.platform.iam.application.dto.OAuthClientResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.dto.SaveOAuthClientCommand;

import java.util.Optional;

public interface OAuthClientRepository {
    PageResult<OAuthClientResult> findAll(String search, int page, int size);

    Optional<OAuthClientResult> findByClientId(String clientId);

    OAuthClientResult create(String clientId, String secretHash, SaveOAuthClientCommand command);

    OAuthClientResult update(String clientId, SaveOAuthClientCommand command);

    void updateSecret(String clientId, String secretHash);

    boolean delete(String clientId);
}
