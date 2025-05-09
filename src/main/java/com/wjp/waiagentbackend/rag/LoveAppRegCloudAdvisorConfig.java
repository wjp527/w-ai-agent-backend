package com.wjp.waiagentbackend.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义基于阿里云知识库服务的 RAG增强顾问
 */
@Configuration
@Slf4j
public class LoveAppRegCloudAdvisorConfig {
    // 阿里云知识库服务API密钥
    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;


    /**
     * 创建RAG增强顾问
     * @return
     */
    @Bean
    public Advisor loveAppRegCloudAdvisor() {
        // 创建DashScopeApi实例
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);

        // 阿里云百炼知识库名称
        final String KNOWLEDGE_INDEX = "恋爱大师";
//        final String KNOWLEDGE_INDEX = "恋爱对象";

        // 创建文档检索器
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                // 创建文档检索选项
                DashScopeDocumentRetrieverOptions.builder()
                        // 设置知识库名称
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build()
        );

        // 创建RAG增强顾问
        return RetrievalAugmentationAdvisor.builder()
                // 设置文档检索器
                .documentRetriever(documentRetriever)
                .build();

    }

}
