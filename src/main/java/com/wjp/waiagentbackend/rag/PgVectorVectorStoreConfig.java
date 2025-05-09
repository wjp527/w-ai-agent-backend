package com.wjp.waiagentbackend.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean("pgVectorVectorStore")
    @Primary
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 创建向量存储
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                // 向量维度，默认值通常为 1536
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                // 相似度计算方式，使用余弦距离
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                // 向量索引类型，使用 HNSW(适合高效近似最近邻搜索)
                .indexType(HNSW)                     // Optional: defaults to HNSW
                // 初始化数据库 Schema，若表不存在自动创建
                .initializeSchema(true)              // Optional: defaults to false
                // 指定数据库 Schema名称
                .schemaName("public")                // Optional: defaults to "public"
                // 指定向量存储表名
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                // 批量插入文档时的最大批次大小，提升性能
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        vectorStore.add(documents);
        return vectorStore;
    }
}
