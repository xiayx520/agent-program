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
}