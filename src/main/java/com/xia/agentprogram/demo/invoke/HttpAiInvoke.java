package com.xia.agentprogram.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {

    private static final String BASE_URL = "https://llm-jg6z5n4odgfj3iho.cn-beijing.maas.aliyuncs.com/api/v1";
    private static final String API_KEY = "sk-ws-H.YHIDMDM.xREv.MEQCIH4tombsqeuAZM9KxfT9hkUE0b78WltGyD_Q-Bw9lgTsAiBjf4U3trm8U_ujef30-Qz5OV1CyMmmVtFKhxr-CwRkFA";
    private static final String MODEL = "qwen3.8-max";

    /**
     * 使用 Hutool 发起多模态对话请求
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提问内容
     * @return 接口响应的 JSON 字符串
     */
    public static String callWithMessage(String systemPrompt, String userPrompt) {
        String url = BASE_URL + "/services/aigc/multimodal-generation/generation";

        // 构建 system 消息
        JSONObject systemContent = new JSONObject();
        systemContent.set("text", systemPrompt);
        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", new JSONArray().put(systemContent));

        // 构建 user 消息
        JSONObject userContent = new JSONObject();
        userContent.set("text", userPrompt);
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", new JSONArray().put(userContent));

        // 构建 messages 数组
        JSONArray messages = new JSONArray();
        messages.put(systemMsg);
        messages.put(userMsg);

        // 构建 input
        JSONObject input = new JSONObject();
        input.set("messages", messages);

        // 构建 parameters
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");

        // 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        body.set("input", input);
        body.set("parameters", parameters);

        // 发起 POST 请求（使用 try-with-resources 自动关闭连接资源）
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(body))
                .execute()) {
            return response.body();
        }
    }

    public static void main(String[] args) {
        String result = callWithMessage("You are a helpful assistant.", "你是谁？");
        System.out.println(JSONUtil.formatJsonStr(result));
    }
}
