package com.wjp.waiagentbackend.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import static com.wjp.waiagentbackend.rag.LoveAppContextualQueryAugmenterFactory.createInstance;

/**
 * 自定义 RAG 检索增强顾问的工厂
 * @Author: wjp
 */
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 创建一个定制的LoveApp问答增强顾问实例
     * @param vectorStore 向量存储实例，用于文档的向量检索
     * @param status 文档过滤状态值，用于筛选特定状态的文档
     * @return 构建完成的RetrievalAugmentationAdvisor实例
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 创建查询表达式[过滤特定状态的文档]
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // 创建文档检索器
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                // 向量存储
                .vectorStore(vectorStore)
                // 检索参数 过滤条件
                .filterExpression(expression)
                // 相似度阈值
                .similarityThreshold(0.5)
                // 返回文档数量
                .topK(3)
                .build();


        // 创建检索增强顾问
        return RetrievalAugmentationAdvisor.builder()
                // 文档检索器
                .documentRetriever(documentRetriever)
                // 文档增强器
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();

    }

}
