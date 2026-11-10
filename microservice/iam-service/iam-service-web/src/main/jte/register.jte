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
                <input name="password" type="password" autocomplete="new-password" required>
            </label>
            <label>
                ${page.t("confirmPassword")}
                <input name="confirmPassword" type="password" autocomplete="new-password" required>
            </label>
        </div>
        <label>
            ${page.t("locale")}
            <select name="locale">
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
            <span aria-hidden="true">
                ↗
            </span>
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
