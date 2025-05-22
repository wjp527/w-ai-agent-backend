package com.wjp.waiagentbackend.agent;

import cn.hutool.core.util.StrUtil;
import com.wjp.waiagentbackend.agent.model.AgentState;
import com.wjp.waiagentbackend.exception.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 智能体基类，定义基本信息和多步骤执行流程
 *
 * 提供状态转换、内存管理和基于步骤的状态执行循环的基础功能
 * 子类必须实现step方法
 * @author wjp
 */
@Data
@Slf4j
public abstract class BaseAgent {
    /**
     * 核心属性
     */
    private String name;

    /**
     * 提示词
     */
    private String systemPrompt;
    private String nextStepPrompt;

    /**
     * 状态
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 最大调用上限
     */
    private int maxSteps = 10;

    /**
     * 当前调用
     */
    private int currentStep = 0;

    /**
     * LLM (大模型)
     */
    private ChatClient chatClient;

    /**
     * Memory (需要自定义维护会话上下文)
     */
    private List<Message> messageList = new ArrayList<>();


    public String run(String userPrompt) {
        // 参数校验
        if(!state.equals(AgentState.IDLE)) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }

        if(StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 更新状态
        state = AgentState.RUNNING;
        // 记录上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0;i < maxSteps && state != AgentState.FINISHED;i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);

                // 单步执行
                String stepResult = step();
                String result = "Step" + stepNumber + ": " + stepResult;
                // 保存结果
                results.add(result);
            }
            // 检查是否超出步骤限制
            if(currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error Executing agent ", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }


    }

    /**
     * 执行单一步骤
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法用来清理资源
    }
}
