package com.wjp.waiagentbackend.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wjp.waiagentbackend.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act方法，可以用作创建实例的父类
 * 实现工具调用能力的智能体
 *
 * @author wjp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存了工具调用信息的模版
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用内置 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1. 校验提示词，拼接用户提示词
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 2. 调用 AI 大模型，获取工具工具调用结果
        // 获取当前上下文列表
        List<Message> messageList = getMessageList();
        // 发送给AI的提示词
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient()
                    // 提示词
                    .prompt(prompt)
                    // 系统预设
                    .system(getSystemPrompt())
                    // 工具调用
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            // 记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;

            // 3. 解析工具调用结果: 获取要调用的工具
            // 获取 AI输出的文本 【助手消息】
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            // 获取到工具调用列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了" + toolCallList.size() + "个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称: %s,参数: %s",
                            toolCall.name(),
                            toolCall.arguments()))
                    .collect(Collectors.joining("\n"));

            log.info(toolCallInfo);

            if(toolCallInfo.isEmpty()) {
                // 只有不同调用工具时，才记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }

        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage())
            );
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     * @return 执行结果
     */
    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if(terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
        }

        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具" + response.name() + " 完成了他的任务! 结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);

        return results;
    }
}
