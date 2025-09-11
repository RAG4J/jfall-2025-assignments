package org.rag4j.webapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

/**
 * Interceptor that redirects users to the token page when there's a configuration mismatch
 * and the embabel profile is active.
 */
@Component
public class ConfigurationMismatchInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationMismatchInterceptor.class);
    
    private final Environment environment;
    private final ConfigurationMismatchHandler mismatchHandler;
    
    public ConfigurationMismatchInterceptor(
            Environment environment,
            ConfigurationMismatchHandler mismatchHandler) {
        this.environment = environment;
        this.mismatchHandler = mismatchHandler;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        // Skip for static resources and token page itself
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/static/") || 
            requestPath.startsWith("/css/") || 
            requestPath.startsWith("/js/") ||
            requestPath.startsWith("/images/") ||
            requestPath.equals("/token") ||
            requestPath.startsWith("/actuator/")) {
            return true;
        }
        
        // Only check when embabel profile is active
        if (!Arrays.asList(environment.getActiveProfiles()).contains("embabel")) {
            return true;
        }
        
        // Check for configuration mismatch
        if (mismatchHandler.hasConfigurationMismatch()) {
            logger.debug("Configuration mismatch detected, redirecting {} to token page", requestPath);
            
            // Build redirect URL with error parameter
            String redirectUrl = UriComponentsBuilder.fromPath("/token")
                    .queryParam("configError", "true")
                    .toUriString();
            
            response.sendRedirect(redirectUrl);
            return false;
        }
        
        return true;
    }
}
