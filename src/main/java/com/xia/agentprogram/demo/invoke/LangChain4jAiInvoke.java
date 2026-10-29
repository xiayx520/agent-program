package com.xia.agentprogram.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenLanguageModel;

public class LangChain4jAiInvoke {

    public static void main(String[] args) {
        QwenLanguageModel model = QwenLanguageModel.builder()
                .apiKey(TestApiKey.apiKey)
                .modelName("qwen-flash")
                .build();
        String answer = model.generate("你好").content();
        System.out.println(answer); // Hello World
    }
}
