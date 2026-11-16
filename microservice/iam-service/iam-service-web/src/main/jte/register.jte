@import kh.edu.istad.platform.web.Page
@param Page page
@template.layout(page=page,title=page.t("register.title"),activeTab="register",body=@`
    <form method="post" action="/register" class="auth-form">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <div class="form-row">
            <label>
                ${page.t("givenName")}
                <input name="givenName" type="text" autocomplete="given-name" required maxlength="100" value="${page.v("givenName")}">
            </label>
            <label>
                ${page.t("familyName")}
                <input name="familyName" type="text" autocomplete="family-name" required maxlength="100" value="${page.v("familyName")}">
            </label>
        </div>
        <label>
            ${page.t("username")}
            <input name="username" type="text" autocomplete="username" required maxlength="50" value="${page.v("username")}">
        </label>
        <label>
            ${page.t("email")}
            <input name="email" type="email" autocomplete="email" required maxlength="255" value="${page.v("email")}">
        </label>
        <div class="form-row">
            <label>
                ${page.t("password")}
                <span class="password-field">
                    <input name="password" type="password" autocomplete="new-password" required>
                    <button type="button" class="password-reveal" data-password-toggle aria-pressed="false" aria-label="${page.t("password.show")}" title="${page.t("password.show")}">
                        <svg class="icon-show" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/></svg>
                        <svg class="icon-hide" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c4.84 0 8.4 3.27 9.94 6.65a1 1 0 0 1 0 .7 10.8 10.8 0 0 1-1.44 2.49M14.08 14.16a3 3 0 0 1-4.24-4.24M17.48 17.5A10.75 10.75 0 0 1 2.06 12.35a1 1 0 0 1 0-.7 10.8 10.8 0 0 1 4.44-5.15M2 2l20 20"/></svg>
                    </button>
                </span>
            </label>
            <label>
                ${page.t("confirmPassword")}
                <span class="password-field">
                    <input name="confirmPassword" type="password" autocomplete="new-password" required>
                    <button type="button" class="password-reveal" data-password-toggle aria-pressed="false" aria-label="${page.t("password.show")}" title="${page.t("password.show")}">
                        <svg class="icon-show" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2.06 12.35a1 1 0 0 1 0-.7 10.75 10.75 0 0 1 19.88 0 1 1 0 0 1 0 .7 10.75 10.75 0 0 1-19.88 0"/><circle cx="12" cy="12" r="3"/></svg>
                        <svg class="icon-hide" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c4.84 0 8.4 3.27 9.94 6.65a1 1 0 0 1 0 .7 10.8 10.8 0 0 1-1.44 2.49M14.08 14.16a3 3 0 0 1-4.24-4.24M17.48 17.5A10.75 10.75 0 0 1 2.06 12.35a1 1 0 0 1 0-.7 10.8 10.8 0 0 1 4.44-5.15M2 2l20 20"/></svg>
                    </button>
                </span>
            </label>
        </div>
        <label>
            ${page.t("locale")}
            <select name="locale" data-ui-select>
                <option value="en" selected="${(page.v("locale").isEmpty() ? page.locale().getLanguage() : page.v("locale")).equals("en")}">
                    English
                </option>
                <option value="km" selected="${(page.v("locale").isEmpty() ? page.locale().getLanguage() : page.v("locale")).equals("km")}">
                    ខ្មែរ
                </option>
            </select>
        </label>
        <button type="submit">
            ${page.t("register.title")}
        </button>
    </form>
    @template.social-login(page=page)
    <p class="auth-note">
        ${page.t("auth.existing")}
        <a href="/login">
            ${page.t("login.title")}
        </a>
    </p>
`)
