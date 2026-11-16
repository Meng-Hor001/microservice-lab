@import kh.edu.istad.platform.web.Page
@param Page page
<script>
    (function () {
        var show = "${page.t("password.show")}", hide = "${page.t("password.hide")}";
        document.querySelectorAll("[data-password-toggle]").forEach(function (b) {
            b.addEventListener("click", function () {
                var input = b.parentElement.querySelector("input");
                var reveal = input.type === "password";
                input.type = reveal ? "text" : "password";
                b.setAttribute("aria-pressed", String(reveal));
                b.setAttribute("aria-label", reveal ? hide : show);
                b.title = reveal ? hide : show;
            });
        });
    })();
</script>
