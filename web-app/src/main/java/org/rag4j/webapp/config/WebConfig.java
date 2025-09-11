package org.rag4j.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the application.
 * Registers interceptors and other web-related configurations.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final ConfigurationMismatchInterceptor configurationMismatchInterceptor;
    
    public WebConfig(ConfigurationMismatchInterceptor configurationMismatchInterceptor) {
        this.configurationMismatchInterceptor = configurationMismatchInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(configurationMismatchInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/css/**", "/js/**", "/images/**", "/actuator/**");
    }
}
