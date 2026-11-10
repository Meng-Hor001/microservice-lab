@import kh.edu.istad.platform.web.Page
@param Page page
@param java.util.List<kh.edu.istad.platform.iam.application.dto.IdentityResult> identities
@template.layout(page=page,title=page.t("profile.accounts"),body=@`
    @for(var identity : identities)
        <section>
            <h2>
                ${identity.provider()}
            </h2>
            <p>
                ${identity.displayName()}
            </p>
            <form method="post" action="/profile/accounts/${identity.uuid().toString()}/unlink">
                <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
                <button>
                    ${page.t("common.unlink")}
                </button>
            </form>
        </section>
    @endfor
    @for(var provider : java.util.List.of("google","github","facebook","telegram"))
        <form method="post" action="/profile/accounts/${provider}/link">
            <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
            <button>
                ${page.t("common.link")} ${provider}
            </button>
        </form>
    @endfor
`)
