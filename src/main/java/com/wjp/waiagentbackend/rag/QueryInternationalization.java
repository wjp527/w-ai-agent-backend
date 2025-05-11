package com.wjp.waiagentbackend.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 多语言翻译 （Spring AI自带）
 */
@Component
public class QueryInternationalization {
    private final QueryTransformer queryTransformer;

    /**
     * 初始化
     * @param dashscopeChatModel
     */
    public QueryInternationalization(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("chinese")
                .build();
    }

    public String doQueryTransformer(String prompt) {
        Query query = new Query(prompt);

        Query transform = queryTransformer.transform(query);
        return transform.text();

    }
}
