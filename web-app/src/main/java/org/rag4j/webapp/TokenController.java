package org.rag4j.webapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rag4j.tokens.TokenService;
import org.rag4j.webapp.config.ConfigurationMismatchHandler;
import org.rag4j.tokens.model.TokenCreationResult;
import org.rag4j.tokens.model.TokenStatus;
import org.rag4j.tokens.model.TokenValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Controller
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private final String openAIProxyUrl;
    private final ConfigurationMismatchHandler mismatchHandler;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenController(
            @Value("${openai.proxy.url}") String openAIProxyUrl,
            ConfigurationMismatchHandler mismatchHandler,
            TokenService tokenService
    ) {
        this.openAIProxyUrl = openAIProxyUrl;
        this.mismatchHandler = mismatchHandler;
        this.tokenService = tokenService;
    }

    @GetMapping("/token")
    public String homePage(
            Model model, 
            @RequestParam(value = "configError", required = false) String configError) {
        
        // Check for configuration mismatch error between env var and application.yml
        if ("true".equals(configError) && mismatchHandler.hasConfigurationMismatch()) {
            String errorMessage = mismatchHandler.getConfigurationMismatchError();
            model.addAttribute("error", "⚠️ Configuration Mismatch Detected: " + errorMessage);
            model.addAttribute("configurationError", true);
            
            // Add additional guidance
            model.addAttribute("configErrorDetails", 
                "Please ensure that your environment variable OPENAI_API_KEY matches the " +
                "openai.proxy.token property in application.yml, then restart the application.");
            
            // Clear the error after displaying it once
            mismatchHandler.clearConfigurationMismatchError();
        }
        
        // Check if proxy URL is configured in application.yml
        boolean proxyInConfig = openAIProxyUrl != null && !openAIProxyUrl.trim().isEmpty();
        
        // Check for OPENAI_BASE_URL environment variable (proxy via env var)
        String openAIBaseUrl = System.getenv("OPENAI_BASE_URL");
        boolean proxyInEnvVar = openAIBaseUrl != null && !openAIBaseUrl.trim().isEmpty();
        
        String openAIApiKey = System.getenv("OPENAI_API_KEY");
        boolean hasApiKey = openAIApiKey != null && !openAIApiKey.trim().isEmpty();
        
        model.addAttribute("proxyConfigured", proxyInConfig);
        model.addAttribute("proxyInEnvVar", proxyInEnvVar);
        
        // Scenario 1: No proxy at all - guide to use personal OpenAI API key
        if (!proxyInConfig && !proxyInEnvVar) {
            if (hasApiKey) {
                model.addAttribute("success", "✅ Using your own OpenAI API key from OPENAI_API_KEY environment variable.");
                model.addAttribute("directOpenAIMode", true);
            } else {
                model.addAttribute("info", 
                    "ℹ️ No proxy configured. To use your own OpenAI API key:\n" +
                    "1. Set the OPENAI_API_KEY environment variable\n" +
                    "2. Restart the application\n\n" +
                    "Example (Mac/Linux):\n" +
                    "export OPENAI_API_KEY='your-api-key-here'\n\n" +
                    "Example (Windows):\n" +
                    "set OPENAI_API_KEY=your-api-key-here");
                model.addAttribute("showEnvVarInstructions", true);
            }
            return "token";
        }
        
        // Scenario 2: Proxy via OPENAI_BASE_URL + OPENAI_API_KEY (manual token management)
        if (!proxyInConfig) {
            model.addAttribute("envVarProxyMode", true);
            model.addAttribute("proxyUrl", openAIBaseUrl);
            
            if (hasApiKey) {
                // Validate the token from OPENAI_API_KEY
                TokenValidationResult validation = tokenService.validateToken(openAIApiKey);
                
                if (validation.isValid()) {
                    model.addAttribute("success", 
                        String.format("✅ Using proxy token from OPENAI_API_KEY environment variable. Valid for %d more minutes.", 
                        validation.getMinutesRemaining()));
                    model.addAttribute("tokenInfo", validation);
                } else {
                    model.addAttribute("warning", 
                        "⚠️ Token in OPENAI_API_KEY is invalid or expired. To get a new token:\n" +
                        "1. Use the form below to request a new token\n" +
                        "2. Copy the generated token\n" +
                        "3. Update your OPENAI_API_KEY environment variable:\n" +
                        "   export OPENAI_API_KEY='new-token-here'\n" +
                        "4. Restart the application");
                    model.addAttribute("tokenExpired", true);
                }
            } else {
                model.addAttribute("error", 
                    "❌ OPENAI_BASE_URL is set but OPENAI_API_KEY is missing.\n" +
                    "Use the form below to request a token, then set:\n" +
                    "export OPENAI_API_KEY='your-token-here'");
            }
            return "token";
        }
        
        // Scenario 3: Proxy in application.yml with auto-managed tokens
        TokenStatus status = tokenService.getTokenStatus();
        model.addAttribute("tokenStatus", status);
        model.addAttribute("cacheEnabled", true);
        model.addAttribute("configProxyMode", true);
        
        if (status.isValid()) {
            model.addAttribute("success", String.format("✅ Auto-managed token is valid for %d more minutes (user: %s)", 
                status.getMinutesRemaining(), status.getUserId()));
            model.addAttribute("autoManaged", true);
        } else {
            if (status.getErrorMessage() != null) {
                if (status.getErrorMessage().equals("Token expired")) {
                    model.addAttribute("warning", "⚠️ Cached token expired. Will auto-refresh on next request if password is configured.");
                } else {
                    model.addAttribute("info", "ℹ️ " + status.getErrorMessage() + " - You can manually request a token below or configure a password for auto-fetch.");
                }
            }
        }
        return "token";
    }

    @PostMapping("/token")
    public String handleFetchToken(
            @Validated @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            Model model) {
        
        // Determine which proxy URL to use (config takes precedence over env var)
        String effectiveProxyUrl = openAIProxyUrl;
        if (effectiveProxyUrl == null || effectiveProxyUrl.trim().isEmpty()) {
            String openAIBaseUrl = System.getenv("OPENAI_BASE_URL");
            if (openAIBaseUrl != null && !openAIBaseUrl.trim().isEmpty()) {
                effectiveProxyUrl = openAIBaseUrl;
                model.addAttribute("envVarProxyMode", true);
            } else {
                model.addAttribute("error", "❌ Token fetching is not available. No proxy URL is configured. Please use OPENAI_API_KEY environment variable instead.");
                model.addAttribute("showEnvVarInstructions", true);
                return "token";
            }
        }
        
        if (userId == null || userId.trim().isEmpty()) {
            model.addAttribute("error", "You need to provide a username.");
            return "token";
        }
        
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "You need to provide a password.");
            model.addAttribute("userId", userId);
            return "token";
        }
        
        // Sanitize message to prevent XSS
        String sanitizedUserId = htmlEscape(userId);
        logger.info("Received token request for userId: {}", sanitizedUserId);

        // Fetch the token for the provided userId with password
        TokenCreationResult result = fetchTokenForUser(effectiveProxyUrl, sanitizedUserId, password);
        
        if (result.isSuccess()) {
            // Check if using env var proxy mode
            boolean usingEnvVarProxy = (openAIProxyUrl == null || openAIProxyUrl.trim().isEmpty()) && 
                                       System.getenv("OPENAI_BASE_URL") != null;
            
            if (usingEnvVarProxy) {
                // Manual token management - user needs to set env var
                model.addAttribute("confirmation", 
                    "✅ Token successfully created! To use this token:\n" +
                    "1. Copy the token below\n" +
                    "2. Update your OPENAI_API_KEY environment variable:\n" +
                    "   export OPENAI_API_KEY='" + result.getToken() + "'\n" +
                    "3. Restart the application");
                model.addAttribute("obtainedToken", result.getToken());
                model.addAttribute("tokenInfo", result);
                model.addAttribute("manualTokenMode", true);
                model.addAttribute("envVarProxyMode", true);
            } else {
                // Auto-managed token - store in TokenService
                tokenService.setToken(result.getToken(), sanitizedUserId);
                model.addAttribute("confirmation", "✅ Token successfully created and cached! It will be used automatically.");
                model.addAttribute("obtainedToken", result.getToken());
                model.addAttribute("tokenInfo", result);
                model.addAttribute("autoManaged", true);
                model.addAttribute("configProxyMode", true);
                
                // Add current token status to show it's now valid
                TokenStatus status = tokenService.getTokenStatus();
                model.addAttribute("tokenStatus", status);
                model.addAttribute("cacheEnabled", true);
            }
        } else {
            model.addAttribute("error", result.getErrorMessage());
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("proxyConfigured", openAIProxyUrl != null && !openAIProxyUrl.trim().isEmpty());
        return "token";
    }

    private TokenCreationResult fetchTokenForUser(String proxyUrl, String userId, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = proxyUrl + "/token";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user", userId);
            requestBody.put("description", "Workshop participant");
            requestBody.put("password", password);
            requestBody.put("minutes", 180);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.has("error")) {
                return new TokenCreationResult(false, jsonResponse.get("error").asText(), null, 0, 0);
            }
            
            return new TokenCreationResult(
                true,
                null,
                jsonResponse.get("token").asText(),
                jsonResponse.get("expires_at").asLong(),
                jsonResponse.get("expires_in_minutes").asInt()
            );
        } catch (Exception e) {
            logger.error("Error creating token for user: {}", userId, e);
            return new TokenCreationResult(false, "Error creating token: " + e.getMessage(), null, 0, 0);
        }
    }
}
