@import kh.edu.istad.platform.web.Page
@param Page page
<span class="language-switch">
    <a href="?lang=en" lang="en" aria-current="${page.locale().getLanguage().equals("en") ? "true" : "false"}" onclick="event.preventDefault();let u=new URL(location.href);u.searchParams.set('lang','en');location.href=u">
        EN
    </a>
    <a href="?lang=km" lang="km" aria-current="${page.locale().getLanguage().equals("km") ? "true" : "false"}" onclick="event.preventDefault();let u=new URL(location.href);u.searchParams.set('lang','km');location.href=u">
        ខ្មែរ
    </a>
</span>
