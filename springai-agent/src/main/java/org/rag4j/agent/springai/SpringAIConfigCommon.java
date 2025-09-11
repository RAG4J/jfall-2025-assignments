package org.rag4j.agent.springai;

import org.rag4j.agent.core.ConferenceTalksRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableConfigurationProperties({OpenAIConfigProperties.class})
@Profile({"springai", "springai-multi"})
public class SpringAIConfigCommon {
    @Bean
    public ChatModel chatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_1_MINI)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(chatOptions)
                .build();
    }

    @Bean
    public ConferenceTalksRepository conferenceTalksRepository() {
        return new ConferenceTalksRepository();
    }

    @Bean
    public ConferenceTalksTools conferenceTalksTools(ConferenceTalksRepository conferenceTalksRepository) {
        return new ConferenceTalksTools(conferenceTalksRepository);
    }

    @Bean
    public OpenAiApi openAIOkHttpClient(OpenAIConfigProperties props) {
        if (props.getUrl() == null || props.getUrl().isEmpty()) {
            var openAIApiKey = System.getenv("OPENAI_API_KEY");
            if (openAIApiKey == null || openAIApiKey.isEmpty()) {
                throw new IllegalArgumentException("No proxy is configured and no OPENAI_API_KEY environment variable has been set");
            }
            return OpenAiApi.builder()
                    .apiKey(openAIApiKey)
                    .build();
        }

        return OpenAiApi.builder()
                .baseUrl(props.getUrl() + "/openai")
                .apiKey(props.getToken())
                .build();
    }

}
