package org.rag4j.webapp.tokens.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rag4j.webapp.tokens.TokenClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of TokenClient using Spring's RestTemplate for HTTP communication.
 */
@Component
public class RestTemplateTokenClient implements TokenClient {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateTokenClient.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RestTemplateTokenClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    // Constructor for testing with custom RestTemplate
    RestTemplateTokenClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public TokenValidationResponse validateToken(String proxyUrl, String token) {
        try {
            String url = proxyUrl + "/token/validate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            return new TokenValidationResponse(
                jsonResponse.get("valid").asBoolean(),
                jsonResponse.get("minutes_remaining").asInt(),
                jsonResponse.has("user") ? jsonResponse.get("user").asText() : null,
                jsonResponse.has("description") ? jsonResponse.get("description").asText() : null,
                jsonResponse.has("expires_at_iso") ? jsonResponse.get("expires_at_iso").asText() : null,
                null
            );
        } catch (Exception e) {
            logger.error("Error validating token with proxy: {}", proxyUrl, e);
            return new TokenValidationResponse(
                false,
                0,
                null,
                null,
                null,
                "Error validating token: " + e.getMessage()
            );
        }
    }
    
    @Override
    public TokenCreationResponse createToken(String proxyUrl, String userId, String password, int durationMinutes) {
        try {
            String url = proxyUrl + "/token";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user", userId);
            requestBody.put("description", "Workshop participant (auto-fetched)");
            requestBody.put("password", password);
            requestBody.put("minutes", durationMinutes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.has("error")) {
                String error = jsonResponse.get("error").asText();
                logger.error("Error from token service: {}", error);
                return new TokenCreationResponse(false, null, 0, 0, error);
            }
            
            return new TokenCreationResponse(
                true,
                jsonResponse.get("token").asText(),
                jsonResponse.get("expires_at").asLong(),
                jsonResponse.get("expires_in_minutes").asInt(),
                null
            );
        } catch (Exception e) {
            logger.error("Error creating token for user: {}", userId, e);
            return new TokenCreationResponse(
                false,
                null,
                0,
                0,
                "Error creating token: " + e.getMessage()
            );
        }
    }
}
