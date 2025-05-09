package com.wjp.waiagentbackend.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 恋爱大师向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class LoveAppVectorStoreConfig {
    // 注入文档加载器
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    /**
     * 创建一个VectorStore对象，用于存储所有解析后的Document对象
     * @param dashscopeEmbeddingModel
     * @return
     */
    @Bean
    public VectorStore LoveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 创建一个基于内存的向量存储对象
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 加载所有Markdown文档
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();
        // 将所有文档添加到向量存储中
        simpleVectorStore.add(documentList);
        return simpleVectorStore;

    }

}
