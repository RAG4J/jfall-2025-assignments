package org.rag4j.agent;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.rag4j.agent.tools.Tool;
import org.rag4j.agent.tools.ToolRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

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
    public OpenAIClient openAIOkHttpClient(PlainAgentOpenAIProperties props) {
        if (props.getUrl() == null || props.getUrl().isEmpty()) {
            var openAIApiKey = System.getenv("OPENAI_API_KEY");
            if (openAIApiKey == null || openAIApiKey.isEmpty()) {
                throw new IllegalArgumentException("No proxy is configured and no OPENAI_API_KEY environment variable has been set");
            }
            return OpenAIOkHttpClient.builder().apiKey(openAIApiKey).build();
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(props.getToken())
                .baseUrl(props.getUrl() + "/openai/v1")
                .build();
    }
}
