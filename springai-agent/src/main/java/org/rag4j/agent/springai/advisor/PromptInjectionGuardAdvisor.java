package org.rag4j.agent.springai.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.regex.Pattern;

public class PromptInjectionGuardAdvisor implements CallAdvisor {

    private final int order;
    private final List<Pattern> riskyPatterns;

    public PromptInjectionGuardAdvisor() {
        this(Ordered.HIGHEST_PRECEDENCE); // run early
    }

    public PromptInjectionGuardAdvisor(int order) {
        this.order = order;

        // Heuristics adapted from common jailbreak patterns
        this.riskyPatterns = List.of(
                // Data-exfil via links or base64
                Pattern.compile("(?i)(follow|read|comply with).*https?://.*(policy|instructions|system|prompt)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)base64\\b.{0,40}\\b(decode|decoding)", Pattern.CASE_INSENSITIVE),
                // Token smuggling / “start output with” instruction takeover
                Pattern.compile("(?i)start\\s+your\\s+output\\s+with\\s+\"?[<\\[{(]", Pattern.CASE_INSENSITIVE),
                // “do anything now” / DAN-style attacks
                Pattern.compile("(?i)do\\s+anything\\s+now|DAN\\b", Pattern.CASE_INSENSITIVE),
                // Common prompt injection patterns
                Pattern.compile("(?i).*ignore\\s+(all|previous|above).*instructions.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*forget\\s+(everything|all|previous).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*you\\s+are\\s+now.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*act\\s+as\\s+(if|though).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*pretend\\s+(to\\s+be|you\\s+are).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*system\\s*:.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*\\[\\s*system\\s*\\].*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*<\\s*system\\s*>.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*override\\s+(previous|system).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i).*new\\s+role.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public String getName() {
        return "PromptInjectionGuardAdvisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        var userText = request.prompt().getUserMessage().getText();
        var hit = patternHit(userText);

        if (hit == null) {
            // No risks detected → continue normally
            return chain.nextCall(request);
        }

        var assistant = new AssistantMessage("""
                {
                    "reasoning": "I can’t comply with instructions that try to override system rules or extract hidden prompts.",
                    "selection": "BLOCKED"
                }
                """);

        var response = new ChatResponse(
                List.of(new Generation(assistant)),
                ChatResponseMetadata.builder().model("guard/PromptInjectionGuardAdvisor").build()
        );

        return ChatClientResponse.builder()
                .chatResponse(response)
                .build();
    }

    private Pattern patternHit(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (var p : riskyPatterns) {
            if (p.matcher(text).find()) {
                return p;
            }
        }
        return null;
    }

}
