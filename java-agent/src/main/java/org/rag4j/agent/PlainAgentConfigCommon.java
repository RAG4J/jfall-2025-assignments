package org.rag4j.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.rag4j.agent.core.TokenProvider;
import org.rag4j.agent.tools.Tool;
import org.rag4j.agent.tools.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties({PlainAgentOpenAIProperties.class, PlainAgentReasoningConfigProperties.class, PlainAgentMemoryConfigProperties.class})
@Profile({"plain","plain-multi"})
public class PlainAgentConfigCommon {
    @Bean
    public ConferenceTalksRepository conferenceTalksRepository() {
        return new ConferenceTalksRepository();
    }

    @Bean(name = "toolRegistry")
    public ToolRegistry toolRegistry(ConferenceTalksRepository conferenceTalksRepository) {
        List<Tool> tools = List.of();

        return new ToolRegistry(tools);
    }


    @Bean
    public OpenAIClient openAIOkHttpClient(
            PlainAgentOpenAIProperties props,
            @Autowired(required = false) TokenProvider tokenProvider) {
        
        if (props.getUrl() == null || props.getUrl().isEmpty()) {
            var openAIApiKey = System.getenv("OPENAI_API_KEY");
            if (openAIApiKey == null || openAIApiKey.isEmpty()) {
                throw new IllegalArgumentException("No proxy is configured and no OPENAI_API_KEY environment variable has been set");
            }
            return OpenAIOkHttpClient.builder().apiKey(openAIApiKey).build();
        }
        
        // Priority 1: Use TokenProvider if available (dynamic token management)
        if (tokenProvider != null) {
            Optional<String> token = tokenProvider.getCurrentToken();
            if (token.isPresent()) {
                return OpenAIOkHttpClient.builder()
                        .apiKey(token.get())
                        .baseUrl(props.getUrl() + "/openai/v1")
                        .build();
            }
        }
        
        // Priority 2: Use static token from properties
        if (props.getToken() != null && !props.getToken().isEmpty()) {
            return OpenAIOkHttpClient.builder()
                    .apiKey(props.getToken())
                    .baseUrl(props.getUrl() + "/openai/v1")
                    .build();
        }
        
        throw new IllegalArgumentException("Proxy is configured but no token is available. " +
                "Either configure openai.proxy.password for auto-fetch or set openai.proxy.token manually.");
    }
}
