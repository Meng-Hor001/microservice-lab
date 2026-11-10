package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.*;
import kh.edu.istad.platform.iam.application.usecase.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

import java.util.*;

@Component
public class FederatedAccountLinker {
    private final LinkIdentityUseCase link;
    private final LoadUserForAuthenticationUseCase load;

    public FederatedAccountLinker(LinkIdentityUseCase link, LoadUserForAuthenticationUseCase load) {
        this.link = link;
        this.load = load;
    }

    public boolean pending(String provider) {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes r)) return false;
        var s = r.getRequest().getSession(false);
        if (s == null) return false;
        Object intent = s.getAttribute("iam.link");
        return intent instanceof Map<?, ?> m && provider.equals(m.get("provider")) && m.get("expires") instanceof Long expires && expires > System.currentTimeMillis();
    }

    public AuthUserResult linkOrProvision(FederatedIdentityCommand command, java.util.function.Supplier<AuthUserResult> provision) {
        String provider = command.provider().name().toLowerCase(Locale.ROOT);
        if (!pending(provider)) return provision.get();
        var r = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        var s = r.getRequest().getSession(false);
        var intent = (Map<?, ?>) s.getAttribute("iam.link");
        s.removeAttribute("iam.link");
        var user = load.execute((String) intent.get("username"));
        if (!user.uuid().toString().equals(intent.get("uuid")) || !user.enabled() || !user.accountNonLocked())
            throw new IllegalArgumentException("Account unavailable");
        link.execute(user.uuid(), command);
        return user;
    }
}
