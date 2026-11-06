package com.xia.agentprogram.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    public ChatClientRequest before(ChatClientRequest chatClientRequest) {
        String DEFAULT_RE2_ADVISE_TEMPLATE = """
                {re2_input_query}
                Read the question again: {re2_input_query}
                """;

        String augmentedUserText = PromptTemplate.builder()
                .template(DEFAULT_RE2_ADVISE_TEMPLATE)
                .variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
                .build()
                .render();

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return callAdvisorChain.nextCall(before(chatClientRequest));
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return streamAdvisorChain.nextStream(before(chatClientRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
