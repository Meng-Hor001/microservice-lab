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
            <span class="password-field">
                <input name="password" type="password" autocomplete="current-password" required placeholder="${page.t("login.passwordPlaceholder")}">
                <button type="button" class="password-reveal" data-password-toggle aria-pressed="false" aria-label="${page.t("password.show")}" title="${page.t("password.show")}">
                    <svg class="icon-show" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg class="icon-hide" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c4.84 0 8.4 3.27 9.94 6.65a1 1 0 0 1 0 .7 10.8 10.8 0 0 1-1.44 2.49M14.08 14.16a3 3 0 0 1-4.24-4.24M17.48 17.5A10.75 10.75 0 0 1 2.06 12.35a1 1 0 0 1 0-.7 10.8 10.8 0 0 1 4.44-5.15M2 2l20 20"/></svg>
                </button>
            </span>
        </label>
        <button type="submit">
            ${page.t("login.title")}
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
