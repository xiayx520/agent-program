package com.xia.agentprogram.agent;


import com.xia.agentprogram.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体基类，定义基本信息和多步骤执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 智能体名称
    private String name;
    // 系统提示
    private String systemPrompt;
    // 下一步提示
    private String nextStepPrompt;
    // 智能体状态
    private AgentState state = AgentState.IDLE;
    // 最大步骤数
    private int maxSteps = 20;
    // 当前步骤数
    private int currentStep = 0;
    // 消息列表
    private List<Message> messageList = new ArrayList<>();
    // 模型客户端
    private ChatClient chatClient;


    /**
     * 运行智能体，执行多步骤流程
     */
    public String run(String userPrompt) {
        try {
            log.info("Starting agent {}", name);
            if (this.state != AgentState.IDLE) {
                throw new RuntimeException("Cannot run agent from state: " + this.state);
            }
            if (StringUtil.isBlank(userPrompt)) {
                throw new RuntimeException("Cannot run agent with empty user prompt");
            }

            state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));
            List<String> results = new ArrayList<>();

            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++){
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);

                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (RuntimeException e) {
            state = AgentState.ERROR;
            log.error("Agent {} 执行错误: {}", name, e.getMessage());
            return "Agent " + name + " 执行错误: " + e.getMessage();
        }finally {
            cleanup();
        }
    }


    /**
     * 智能体的一步执行步骤，具体步骤由子类实现
     */
    public abstract String step();


    /**
     * 清理操作，可以在步骤完成后执行一些清理工作
     */
    protected void cleanup() {
        log.info("Cleaning up agent {}", name);
        messageList.clear();
        state = AgentState.IDLE;
        currentStep = 0;
        chatClient = null;
        log.info("Agent {} cleaned up", name);
    }

}
