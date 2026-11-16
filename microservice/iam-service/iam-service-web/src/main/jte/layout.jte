@import kh.edu.istad.platform.web.Page
@import gg.jte.Content
@param Page page
@param String title
@param Content body
@param String activeTab = ""
<!doctype html>
<html lang="${page.locale().getLanguage()}">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <meta name="color-scheme" content="light dark">
        <title>
            ${title}
        </title>
        <script>
            try { var t = localStorage.getItem("iam.theme"); if (t === "light" || t === "dark") document.documentElement.setAttribute("data-theme", t) } catch (e) {}
        </script>
        <link rel="stylesheet" href="/iam.css">
    </head>
    <body class="${activeTab.isEmpty() ? "account-page" : "auth-page"}">
        @if(!activeTab.isEmpty())
            <div class="auth">
                <div class="bubbles" aria-hidden="true">
                    <span></span><span></span><span></span><span></span><span></span><span></span><span></span>
                </div>
                <div class="auth-panel">
                    <header class="auth-top">
                        <span class="auth-controls">
                            @template.theme-switch(page=page)
                            @template.language-switch(page=page)
                        </span>
                    </header>
                    <main class="auth-main">
                        <div class="auth-card">
                        <nav class="auth-tabs" aria-label="${page.t("common.navigation")}">
                            <a href="/login" class="${activeTab.equals("login") ? "selected" : ""}" aria-current="${activeTab.equals("login") ? "page" : "false"}">
                                ${page.t("login.title")}
                            </a>
                            <a href="/register" class="${activeTab.equals("register") ? "selected" : ""}" aria-current="${activeTab.equals("register") ? "page" : "false"}">
                                ${page.t("auth.signup")}
                            </a>
                        </nav>
                        <div class="page-heading">
                            <h1>
                                ${title}
                            </h1>
                            <p>
                                ${page.t(activeTab.equals("login") ? "login.subtitle" : "register.subtitle")}
                            </p>
                        </div>
                        @if(!page.notice().isEmpty())
                            <p class="notice" role="status">
                                ${page.t(page.notice())}
                            </p>
                        @endif
                        @if(!page.error().isEmpty())
                            <p class="error" role="alert">
                                ${page.t(page.error())}
                            </p>
                        @endif
                        ${body}
                        </div>
                        <p class="auth-trust">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect width="16" height="11" x="4" y="11" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/></svg>
                            ${page.t("auth.footer")}
                        </p>
                    </main>
                </div>
            </div>
            @template.password-toggle(page=page)
            @template.ui-select()
        @else
            <header>
                <nav aria-label="${page.t("common.navigation")}">
                    <a href="/">
                        ${page.t("profile.title")}
                    </a>
                    <a href="/profile/edit">
                        ${page.t("profile.edit")}
                    </a>
                    <a href="/profile/accounts">
                        ${page.t("profile.accounts")}
                    </a>
                    <a href="/profile/apps">
                        ${page.t("profile.apps")}
                    </a>
                    @template.language-switch(page=page)
                </nav>
            </header>
            <div class="page-shell">
                <main>
                    <div class="page-heading">
                        <span class="eyebrow">
                            ${page.t("app.name")}
                        </span>
                        <h1>
                            ${title}
                        </h1>
                    </div>
                    @if(!page.notice().isEmpty())
                        <p class="notice" role="status">
                            ${page.t(page.notice())}
                        </p>
                    @endif
                    @if(!page.error().isEmpty())
                        <p class="error" role="alert">
                            ${page.t(page.error())}
                        </p>
                    @endif
                    ${body}
                </main>
                <p class="page-footer">
                    ${page.t("auth.footer")}
                </p>
            </div>
        @endif
    </body>
</html>
