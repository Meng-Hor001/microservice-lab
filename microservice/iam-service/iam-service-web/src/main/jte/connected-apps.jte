@import kh.edu.istad.platform.web.Page
@param Page page
@param java.util.List<kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort.App> apps
@template.layout(page=page,title=page.t("profile.apps"),body=@`
    @if(apps.isEmpty())
        <p>
            ${page.t("apps.empty")}
        </p>
    @endif
    @for(var app : apps)
        <section>
            <h2>
                ${app.name()}
            </h2>
            <p>
                ${app.scopes()}
            </p>
            <form method="post" action="/profile/apps/${app.id()}/revoke">
                <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
                <button>
                    ${page.t("apps.revoke")}
                </button>
            </form>
        </section>
    @endfor
`)
