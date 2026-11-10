@import kh.edu.istad.platform.web.Page
@param Page page
@param String clientName
@param String clientId
@param String state
@param java.util.List<String> scopes
@template.layout(page=page,title=page.t("consent.title"),body=@`
    <h2>
        ${clientName}
    </h2>
    <p>
        ${page.t("consent.request")}
    </p>
    <form method="post" action="/oauth2/authorize">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <input type="hidden" name="client_id" value="${clientId}">
        <input type="hidden" name="state" value="${state}">
        @for(var scope : scopes)
            <label>
                <input type="checkbox" name="scope" value="${scope}">
                ${scope}
            </label>
        @endfor
        <button type="submit">
            ${page.t("consent.allow")}
        </button>
    </form>
    <form method="post" action="/oauth2/authorize">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <input type="hidden" name="client_id" value="${clientId}">
        <input type="hidden" name="state" value="${state}">
        <button type="submit">
            ${page.t("consent.cancel")}
        </button>
    </form>
`)
