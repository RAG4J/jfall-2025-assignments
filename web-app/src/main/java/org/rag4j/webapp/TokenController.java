package org.rag4j.webapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rag4j.webapp.config.ConfigurationMismatchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import java.util.Optional;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

@Controller
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private final String openAIProxyUrl;
    private final Optional<String> openAIProxyToken;
    private final ConfigurationMismatchHandler mismatchHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenController(
            @Value("${openai.proxy.url}") String openAIProxyUrl,
            @Value("${openai.proxy.token:#{null}}") Optional<String> openAIProxyToken,
            ConfigurationMismatchHandler mismatchHandler
    ) {
        this.openAIProxyUrl = openAIProxyUrl;
        this.openAIProxyToken = openAIProxyToken;
        this.mismatchHandler = mismatchHandler;
    }

    @GetMapping("/token")
    public String homePage(
            Model model, 
            @RequestParam(value = "configError", required = false) String configError) {
        
        // Check for configuration mismatch error
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
        
        // Check if we have a token and validate it
        if (openAIProxyToken.isPresent() && !openAIProxyToken.get().trim().isEmpty()) {
            TokenValidationResult validation = validateExistingToken();
            model.addAttribute("tokenValidation", validation);
            
            if (validation.isValid()) {
                model.addAttribute("success", String.format("Current token is valid for %d more minutes", validation.getMinutesRemaining()));
            } else {
                model.addAttribute("warning", "Current token is invalid or expired. Please request a new token.");
            }
        } else {
            model.addAttribute("info", "No token configured. Please request a token below.");
        }
        return "token";
    }

    @PostMapping("/token")
    public String handleFetchToken(
            @Validated @RequestParam("userId") String userId,
            @RequestParam("password") String password,
            Model model) {
        
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
        TokenCreationResult result = fetchTokenForUser(sanitizedUserId, password);
        
        if (result.isSuccess()) {
            model.addAttribute("confirmation", "Token successfully created! Copy it and add it to application.yml");
            model.addAttribute("obtainedToken", result.getToken());
            model.addAttribute("tokenInfo", result);
        } else {
            model.addAttribute("error", result.getErrorMessage());
        }
        
        model.addAttribute("userId", userId);
        return "token";
    }

    private TokenValidationResult validateExistingToken() {
        if (openAIProxyToken.isEmpty() || openAIProxyToken.get().trim().isEmpty()) {
            return new TokenValidationResult(false, "No token configured", 0, null, null, null);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = this.openAIProxyUrl + "/token/validate";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openAIProxyToken.get());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            return new TokenValidationResult(
                jsonResponse.get("valid").asBoolean(),
                null,
                jsonResponse.get("minutes_remaining").asInt(),
                jsonResponse.has("user") ? jsonResponse.get("user").asText() : null,
                jsonResponse.has("description") ? jsonResponse.get("description").asText() : null,
                jsonResponse.has("expires_at_iso") ? jsonResponse.get("expires_at_iso").asText() : null
            );
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return new TokenValidationResult(false, "Error validating token: " + e.getMessage(), 0, null, null, null);
        }
    }

    private TokenCreationResult fetchTokenForUser(String userId, String password) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = this.openAIProxyUrl + "/token";

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

    // Data classes for structured responses
    public static class TokenValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final int minutesRemaining;
        private final String user;
        private final String description;
        private final String expiresAtIso;

        public TokenValidationResult(boolean valid, String errorMessage, int minutesRemaining, 
                                   String user, String description, String expiresAtIso) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.minutesRemaining = minutesRemaining;
            this.user = user;
            this.description = description;
            this.expiresAtIso = expiresAtIso;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public int getMinutesRemaining() { return minutesRemaining; }
        public String getUser() { return user; }
        public String getDescription() { return description; }
        public String getExpiresAtIso() { return expiresAtIso; }
    }

    public static class TokenCreationResult {
        private final boolean success;
        private final String errorMessage;
        private final String token;
        private final long expiresAt;
        private final int expiresInMinutes;

        public TokenCreationResult(boolean success, String errorMessage, String token, 
                                 long expiresAt, int expiresInMinutes) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.token = token;
            this.expiresAt = expiresAt;
            this.expiresInMinutes = expiresInMinutes;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getToken() { return token; }
        public long getExpiresAt() { return expiresAt; }
        public int getExpiresInMinutes() { return expiresInMinutes; }
    }
}
