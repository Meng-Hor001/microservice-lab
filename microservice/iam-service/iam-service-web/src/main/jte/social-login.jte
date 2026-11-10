@import kh.edu.istad.platform.web.Page
@param Page page
<div class="divider">
    <span>
        ${page.t("auth.or")}
    </span>
</div>
<div class="social-grid">
    @for(var provider : java.util.List.of("Google", "Github", "Facebook", "Telegram"))
        <a class="social-button" href="/oauth2/authorization/${provider.toLowerCase(java.util.Locale.ROOT)}" aria-label="${page.t("common.continue" + provider)}">
            <span class="provider-icon" aria-hidden="true">
                @template.provider-icon(provider=provider)
            </span>
            <span>
                ${provider.equals("Github") ? "GitHub" : provider}
            </span>
        </a>
    @endfor
</div>
