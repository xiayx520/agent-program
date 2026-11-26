package com.xia.agentprogram.controller;

import com.xia.agentprogram.app.LoveApp;
import com.xia.agentprogram.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/ai")
public class AIController {
    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashScopeChatModel;

    /**
     * 同步调用
     */
    @GetMapping("/love_app/chat/sync")
    public Result<String> doChatWithLoveAppSync(String message, String chatId) {
        if (message == null || chatId == null || message.isEmpty() || chatId.isEmpty())
            return Result.error("message or chatId is empty");
        log.info("doChatWithLoveAppSync message:{}, chatId:{}", message, chatId);
        return Result.success(loveApp.useChat(message, chatId));
    }

    /**
     * 流式调用
     */
    @GetMapping(value = "/love_app/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppStream(String message, String chatId) {
        if (message == null || chatId == null || message.isEmpty() || chatId.isEmpty())
            return Flux.just("message or chatId is empty");
        log.info("doChatWithLoveAppStream message:{}, chatId:{}", message, chatId);
        return loveApp.useChatByStream(message, chatId);
    }


    @GetMapping(value = "/love_app/chat/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(String message, String chatId) {
        if (message == null || chatId == null || message.isEmpty() || chatId.isEmpty())
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("message or chatId is empty")
                    .build());
        log.info("doChatWithLoveAppSSE message:{}, chatId:{}", message, chatId);
        return loveApp.useChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }


    @GetMapping("/love_app/chat/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        if (message == null || chatId == null || message.isEmpty() || chatId.isEmpty()){
            SseEmitter emitter = new SseEmitter(180000L);
            try {
                emitter.send("message or chatId is empty");
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        log.info("doChatWithLoveAppSseEmitter message:{}, chatId:{}", message, chatId);
        SseEmitter emitter = new SseEmitter(180000L);
        loveApp.useChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

}
