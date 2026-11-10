@import kh.edu.istad.platform.web.Page
@param Page page
@template.layout(page=page,title=page.t("login.title"),activeTab="login",body=@`
    <form method="post" action="/login" class="auth-form">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <label>
            ${page.t("login.identifier")}
            <input name="username" type="text" autocomplete="username" required value="${page.v("username")}" placeholder="${page.t("login.identifierPlaceholder")}">
        </label>
        <label>
            ${page.t("password")}
            <input name="password" type="password" autocomplete="current-password" required placeholder="${page.t("login.passwordPlaceholder")}">
        </label>
        <button type="submit">
            ${page.t("login.title")}
            <span aria-hidden="true">
                ↗
            </span>
        </button>
    </form>
    @template.social-login(page=page)
    <p class="auth-note">
        ${page.t("auth.new")}
        <a href="/register">
            ${page.t("auth.signup")}
        </a>
    </p>
`)
