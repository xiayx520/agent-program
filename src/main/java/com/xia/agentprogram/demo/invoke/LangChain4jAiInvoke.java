package com.xia.agentprogram.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenLanguageModel;

public class LangChain4jAiInvoke {

    public static void main(String[] args) {
        QwenLanguageModel model = QwenLanguageModel.builder()
                .apiKey("sk-ws-H.YHIDMDM.xREv.MEQCIH4tombsqeuAZM9KxfT9hkUE0b78WltGyD_Q-Bw9lgTsAiBjf4U3trm8U_ujef30-Qz5OV1CyMmmVtFKhxr-CwRkFA")
                .modelName("qwen-max")
                .build();
        String answer = model.generate("你好").content();
        System.out.println(answer); // Hello World
    }
}
