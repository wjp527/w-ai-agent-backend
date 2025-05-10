package com.wjp.waiagentbackend.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.stereotype.Component;

/**
 * 创建上下文查询增强器的工厂
 */
public class LoveAppContextualQueryAugmenterFactory {

    /**
     * 创建上下文查询增强器实例
     * @return
     */
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系我 https://github.com/wjp527
                """);

        // 创建上下文查询增强器
        return ContextualQueryAugmenter.builder()
                // 设置上下文提示模板
                .allowEmptyContext(false)
                // 设置空上下文时的提示模板
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }

}
