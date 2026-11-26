package com.xia.agentprogram.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现思考和行动两个步骤的智能体
 */


@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 思考步骤，具体思考逻辑由子类实现
     */
    public abstract boolean think();

    /**
     * 行动步骤，具体行动逻辑由子类实现
     */
    public abstract String act();

    /**
     * 智能体的一步，包含思考和行动两个步骤
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                log.info("思考完成 - 无需行动");
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            log.error("步骤执行失败 - {}", e.getMessage());
            return "步骤执行失败 - " + e.getMessage();
        }
    }
}
