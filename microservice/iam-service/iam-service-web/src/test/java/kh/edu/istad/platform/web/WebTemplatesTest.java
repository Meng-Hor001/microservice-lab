package kh.edu.istad.platform.web;

import gg.jte.*;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WebTemplatesTest {
    @Test
    void allTemplatesCompileAndPublicFormsRenderInBothLanguages() throws Exception {
        var engine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/main/jte")), Files.createTempDirectory("iam-jte"), ContentType.Html);
        engine.precompileAll();
        var previews = Path.of("build/previews");
        Files.createDirectories(previews);
        Files.copy(Path.of("src/main/resources/static/iam.css"), previews.resolve("iam.css"), StandardCopyOption.REPLACE_EXISTING);
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        for (var locale : List.of(Locale.ENGLISH, Locale.forLanguageTag("km"))) {
            var page = new Page(messages, locale, "_csrf", "test-csrf", Map.of("username", "<script>"), "");
            for (var name : List.of("login", "register", "change-password")) {
                var output = new StringOutput();
                engine.render(name + ".jte", Map.of("page", page), output);
                Files.writeString(previews.resolve(name + "-" + locale.getLanguage() + ".html"), output.toString());
                assertTrue(output.toString().contains("test-csrf"));
                assertFalse(output.toString().contains("value=\"<script>"));
                assertTrue(output.toString().contains("lang=\"" + locale.getLanguage() + "\""));
            }
            for (var name : List.of("connected-apps", "linked-accounts")) {
                var output = new StringOutput();
                engine.render(name + ".jte", Map.of("page", page, name.equals("connected-apps") ? "apps" : "identities", List.of()), output);
                assertTrue(output.toString().contains("<main>"));
            }
            var user = org.mockito.Mockito.mock(kh.edu.istad.platform.iam.application.dto.AuthUserResult.class);
            var authorities = org.mockito.Mockito.mock(kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult.class);
            org.mockito.Mockito.when(authorities.roles()).thenReturn(List.of());
            org.mockito.Mockito.when(authorities.effectivePermissions()).thenReturn(List.of());
            engine.render("index.jte", Map.of("page", page, "user", user, "authorities", authorities), new StringOutput());
            engine.render("profile-edit.jte", Map.of("page", page, "user", user), new StringOutput());
            var output = new StringOutput();
            engine.render("consent.jte", Map.of("page", page, "clientName", "<script>", "clientId", "test", "state", "state", "scopes", List.of("profile", "email")), output);
            assertTrue(output.toString().contains("&lt;script&gt;"));
        }
    }
}
