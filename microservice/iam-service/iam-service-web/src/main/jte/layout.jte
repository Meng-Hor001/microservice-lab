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
        <meta name="color-scheme" content="dark">
        <title>
            ${title}
        </title>
        <link rel="stylesheet" href="/iam.css">
    </head>
    <body class="${activeTab.isEmpty() ? "account-page" : "auth-page"}">
        <header>
            <nav aria-label="${page.t("common.navigation")}">
                @if(activeTab.isEmpty())
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
                @endif
                <span class="language-switch">
                    <a href="?lang=en" lang="en" aria-current="${page.locale().getLanguage().equals("en") ? "true" : "false"}" onclick="event.preventDefault();let u=new URL(location.href);u.searchParams.set('lang','en');location.href=u">
                        English
                    </a>
                    <span aria-hidden="true">
                        /
                    </span>
                    <a href="?lang=km" lang="km" aria-current="${page.locale().getLanguage().equals("km") ? "true" : "false"}" onclick="event.preventDefault();let u=new URL(location.href);u.searchParams.set('lang','km');location.href=u">
                        ខ្មែរ
                    </a>
                </span>
            </nav>
        </header>
        <div class="page-shell">
            <main>
                @if(!activeTab.isEmpty())
                    <nav class="auth-tabs" aria-label="${page.t("common.navigation")}">
                        <a href="/register" class="${activeTab.equals("register") ? "selected" : ""}" aria-current="${activeTab.equals("register") ? "page" : "false"}">
                            ${page.t("auth.signup")}
                        </a>
                        <a href="/login" class="${activeTab.equals("login") ? "selected" : ""}" aria-current="${activeTab.equals("login") ? "page" : "false"}">
                            ${page.t("login.title")}
                        </a>
                    </nav>
                @endif
                <div class="page-heading">
                    <span class="eyebrow">
                        ${page.t("app.name")}
                    </span>
                    <h1>
                        ${title}
                    </h1>
                    @if(!activeTab.isEmpty())
                        <p>
                            ${page.t(activeTab.equals("login") ? "login.subtitle" : "register.subtitle")}
                        </p>
                    @endif
                </div>
                @if(!page.error().isEmpty())
                    <p class="error" role="alert">
                        ${page.t(page.error())}
                    </p>
                @endif
                ${body}
            </main>
            <p class="page-footer">
                <span class="security-dot" aria-hidden="true">
                </span>
                ${page.t("auth.footer")}
            </p>
        </div>
    </body>
</html>
