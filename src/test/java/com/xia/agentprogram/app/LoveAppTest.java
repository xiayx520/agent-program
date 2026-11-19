package com.xia.agentprogram.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好";
        String answer = loveApp.useChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "我想让另一半（常小娥）更爱我";
        answer = loveApp.useChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.useChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void useChatWithReport() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好，我想让另一半（常小娥）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.useChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void useChatWithRag() {

        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.useChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void useChatWithCloudRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.useChatWithCloudRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void useChatWithPgVector() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.useChatWithPgVector(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void useChatWithRetriever() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.useChatWithRetriever(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void useChatWithCustomAdvisorFactory() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.useChatWithCustomAdvisorFactory(message, chatId);
        Assertions.assertNotNull(answer);
    }
}