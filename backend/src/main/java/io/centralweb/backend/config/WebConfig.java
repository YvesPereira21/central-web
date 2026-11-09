package io.centralweb.backend.config;

import io.centralweb.backend.security.CreationRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CreationRateLimitInterceptor creationRateLimitInterceptor;

    public WebConfig(CreationRateLimitInterceptor creationRateLimitInterceptor) {
        this.creationRateLimitInterceptor = creationRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(creationRateLimitInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //All of url that starts with /uploads/profiles/ will search in a local folder
        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations("file:uploads/profiles/");
    }
}
