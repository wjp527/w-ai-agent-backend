package com.wjp.waiagentbackend.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 * QueryReWriter类负责对原始查询语句进行重写处理
 * 通过整合大模型能力实现查询语句的语义优化和结构转换
 */
@Component
public class QueryReWriter {
    /**
     * 用于转换查询的组件，负责将输入查询转换为特定于数据库的查询格式。
     * 该组件在初始化时通过依赖注入方式设置，且不可变（final修饰）。
     */
    private final QueryTransformer queryTransformer;

    /**
     * 构造函数初始化查询转换器组件
     *
     * @param dashscopeChatModel 大模型服务接口实例
     *        用于构建支持查询重写的对话客户端
     */
    public QueryReWriter(ChatModel dashscopeChatModel) {
        // 构建对话客户端
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // 初始化查询转换器
        queryTransformer = RewriteQueryTransformer.builder()
                // 注入客户端构建器
                .chatClientBuilder(builder)
                // 最终构建 QueryTransformer 实例
                .build();
    }


    /**
     * 执行查询语句重写操作
     *
     * @param prompt 原始用户查询语句
     * @return 转换优化后的查询语句
     */
    public String doQueryRewrite(String prompt) {
        // 创建查询对象
        Query query = new Query(prompt);
        // 查询查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 返回重写后的查询语句
        return transformedQuery.text();
    }


}
