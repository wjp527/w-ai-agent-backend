package com.wjp.waiagentbackend.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用 AI 大模型(阿里)
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;


    @Override
    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("您好，您在干嘛"))
//                .getResult()
//                .getOutput();
//        System.out.println(assistantMessage.getText());
    }
}
