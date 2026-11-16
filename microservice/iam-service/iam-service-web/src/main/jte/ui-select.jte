<script>
    /* shadcn-style Select for server-rendered pages: replaces the native list with a styled
       popover but keeps the original <select> (hidden) as the value that gets submitted. */
    (function () {
        var CHEVRON = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m6 9 6 6 6-6"/></svg>';
        var CHECK = '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6 9 17l-5-5"/></svg>';
        var uid = 0;

        document.querySelectorAll("select[data-ui-select]").forEach(function (select) {
            var id = "ui-select-" + (++uid);
            var root = document.createElement("span");
            root.className = "ui-select";
            select.parentNode.insertBefore(root, select);

            var trigger = document.createElement("button");
            trigger.type = "button";
            trigger.className = "ui-select-trigger";
            trigger.setAttribute("aria-haspopup", "listbox");
            trigger.setAttribute("aria-expanded", "false");
            trigger.setAttribute("aria-controls", id);
            trigger.innerHTML = '<span class="ui-select-value"></span>' + CHEVRON;

            var list = document.createElement("span");
            list.className = "ui-select-content";
            list.id = id;
            list.setAttribute("role", "listbox");
            list.tabIndex = -1;
            list.hidden = true;

            Array.prototype.forEach.call(select.options, function (opt, i) {
                var item = document.createElement("span");
                item.className = "ui-select-item";
                item.setAttribute("role", "option");
                item.id = id + "-" + i;
                item.dataset.index = i;
                if (opt.lang) item.lang = opt.lang;
                item.innerHTML = '<span class="ui-select-label"></span><span class="ui-select-check">' + CHECK + '</span>';
                item.firstChild.textContent = opt.textContent.trim();
                list.appendChild(item);
            });

            // Button first so the surrounding <label> activates it, not the hidden select.
            root.appendChild(trigger);
            root.appendChild(list);
            root.appendChild(select);
            select.tabIndex = -1;
            select.setAttribute("aria-hidden", "true");
            select.classList.add("ui-select-native");

            var items = list.querySelectorAll(".ui-select-item");
            var active = select.selectedIndex;

            function sync() {
                trigger.firstChild.textContent = select.options[select.selectedIndex].textContent.trim();
                items.forEach(function (it, i) { it.setAttribute("aria-selected", String(i === select.selectedIndex)) });
            }
            function highlight(i) {
                active = (i + items.length) % items.length;
                items.forEach(function (it, j) { it.toggleAttribute("data-highlighted", j === active) });
                list.setAttribute("aria-activedescendant", items[active].id);
            }
            function open() {
                list.hidden = false;
                trigger.setAttribute("aria-expanded", "true");
                highlight(select.selectedIndex);
                list.focus();
            }
            function close(focusTrigger) {
                list.hidden = true;
                trigger.setAttribute("aria-expanded", "false");
                if (focusTrigger) trigger.focus();
            }
            function choose(i) {
                select.selectedIndex = i;
                select.dispatchEvent(new Event("change", { bubbles: true }));
                sync();
                close(true);
            }

            trigger.addEventListener("click", function (e) {
                e.preventDefault();
                list.hidden ? open() : close(true);
            });
            trigger.addEventListener("keydown", function (e) {
                if (e.key === "ArrowDown" || e.key === "ArrowUp") { e.preventDefault(); open() }
            });
            list.addEventListener("click", function (e) {
                // preventDefault stops the wrapping <label> from re-clicking the trigger.
                e.preventDefault();
                var item = e.target.closest(".ui-select-item");
                if (item) choose(Number(item.dataset.index));
            });
            list.addEventListener("mousemove", function (e) {
                var item = e.target.closest(".ui-select-item");
                if (item) highlight(Number(item.dataset.index));
            });
            list.addEventListener("keydown", function (e) {
                if (e.key === "ArrowDown") { e.preventDefault(); highlight(active + 1) }
                else if (e.key === "ArrowUp") { e.preventDefault(); highlight(active - 1) }
                else if (e.key === "Home") { e.preventDefault(); highlight(0) }
                else if (e.key === "End") { e.preventDefault(); highlight(items.length - 1) }
                else if (e.key === "Enter" || e.key === " ") { e.preventDefault(); choose(active) }
                else if (e.key === "Escape") { e.preventDefault(); close(true) }
                else if (e.key === "Tab") { close(false) }
            });
            document.addEventListener("pointerdown", function (e) {
                if (!list.hidden && !root.contains(e.target)) close(false);
            });
            sync();
        });
    })();
</script>
