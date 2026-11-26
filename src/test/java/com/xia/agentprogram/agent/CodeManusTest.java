package com.xia.agentprogram.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodeManusTest {

    @Resource
    private CodeManus codeManus;

    @Test
    void run() {
        String userPrompt = """  
                我想去重庆旅游，但是不知道该去哪里玩，
                请你帮我制定一份详细的旅游计划，并结合一些网络图片，
                以 PDF 格式输出""";
        String answer = codeManus.run(userPrompt);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

}