@import kh.edu.istad.platform.web.Page
@param Page page
<span class="theme-switch" role="group" aria-label="${page.t("theme.label")}">
    <button type="button" data-theme-choice="light" aria-label="${page.t("theme.light")}" title="${page.t("theme.light")}" aria-pressed="false">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/></svg>
    </button>
    <button type="button" data-theme-choice="dark" aria-label="${page.t("theme.dark")}" title="${page.t("theme.dark")}" aria-pressed="false">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401"/></svg>
    </button>
    <button type="button" data-theme-choice="system" aria-label="${page.t("theme.system")}" title="${page.t("theme.system")}" aria-pressed="true">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect width="20" height="14" x="2" y="3" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
    </button>
</span>
<script>
    (function () {
        var KEY = "iam.theme", root = document.documentElement;
        var buttons = document.querySelectorAll("[data-theme-choice]");
        function current() {
            try { return localStorage.getItem(KEY) || "system" } catch (e) { return "system" }
        }
        function apply(choice) {
            if (choice === "light" || choice === "dark") root.setAttribute("data-theme", choice);
            else root.removeAttribute("data-theme");
            buttons.forEach(function (b) { b.setAttribute("aria-pressed", String(b.dataset.themeChoice === choice)) });
        }
        buttons.forEach(function (b) {
            b.addEventListener("click", function () {
                var choice = b.dataset.themeChoice;
                try { localStorage.setItem(KEY, choice) } catch (e) {}
                apply(choice);
            });
        });
        apply(current());
    })();
</script>
