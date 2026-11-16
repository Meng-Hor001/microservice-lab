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

    /**
     * Serves the React account dashboard under /me/. Real files (JS, CSS, icons) are returned as-is;
     * any other path falls back to index.html so React Router can handle client-side routes like
     * /me/overview or /me/sessions on refresh or direct links.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var index = new org.springframework.core.io.ClassPathResource("static/me/index.html");
        registry.addResourceHandler("/me", "/me/", "/me/**")
                .addResourceLocations("classpath:/static/me/")
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(
                            String resourcePath, org.springframework.core.io.Resource location) throws java.io.IOException {
                        var file = location.createRelative(resourcePath);
                        if (!resourcePath.isEmpty() && file.exists() && file.isReadable()) {
                            return file;
                        }
                        return index.exists() ? index : null;
                    }
                });
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var i = new LocaleChangeInterceptor();
        i.setParamName("lang");
        registry.addInterceptor(i);
    }
}
