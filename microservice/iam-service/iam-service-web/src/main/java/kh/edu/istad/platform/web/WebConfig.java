package kh.edu.istad.platform.web;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Bean
    public gg.jte.TemplateEngine templateEngine() throws java.io.IOException {
        return gg.jte.TemplateEngine.create(new gg.jte.resolve.ResourceCodeResolver("templates"), java.nio.file.Files.createTempDirectory("iam-jte-"), gg.jte.ContentType.Html);
    }

    @Bean
    public kh.edu.istad.platform.iam.application.usecase.ListConnectedAppsUseCase listConnectedApps(kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort port) {
        return new kh.edu.istad.platform.iam.application.usecase.ListConnectedAppsUseCase(port);
    }

    @Bean
    public kh.edu.istad.platform.iam.application.usecase.RevokeConnectedAppUseCase revokeConnectedApp(kh.edu.istad.platform.iam.application.port.out.ConnectedAppsPort port) {
        return new kh.edu.istad.platform.iam.application.usecase.RevokeConnectedAppUseCase(port);
    }

    @Bean
    public LocaleResolver localeResolver() {
        var r = new SessionLocaleResolver();
        r.setDefaultLocale(java.util.Locale.ENGLISH);
        return r;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var i = new LocaleChangeInterceptor();
        i.setParamName("lang");
        registry.addInterceptor(i);
    }
}
