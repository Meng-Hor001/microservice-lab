@import kh.edu.istad.platform.web.Page
@param Page page
@template.layout(page=page,title=page.t("profile.password"),body=@`
    <form method="post" action="/profile/password" class="auth-form">
        <input type="hidden" name="${page.csrfName()}" value="${page.csrfToken()}">
        <label>
            ${page.t("currentPassword")}
            <input name="currentPassword" type="password" autocomplete="current-password" required value="${""}">
        </label>
        <label>
            ${page.t("newPassword")}
            <input name="newPassword" type="password" autocomplete="new-password" required value="${""}">
        </label>
        <label>
            ${page.t("confirmPassword")}
            <input name="confirmPassword" type="password" autocomplete="new-password" required value="${""}">
        </label>
        <button>
            ${page.t("common.save")}
        </button>
    </form>
`)
