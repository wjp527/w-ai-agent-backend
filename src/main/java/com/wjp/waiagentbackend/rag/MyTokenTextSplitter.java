package com.wjp.waiagentbackend.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MyTokenTextSplitter 是一个 Spring 组件类，用于处理文档的文本分割操作。
 * 提供了两种文档分割策略：默认配置和自定义配置。
 */
@Component
class MyTokenTextSplitter {

    /**
     * 使用默认配置对文档列表进行分割处理。
     *
     * @param documents 需要被分割的原始文档列表
     * @return 分割处理后的文档列表，每个文档包含不超过默认限制的 token 数量
     */
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    /**
     * 使用自定义配置对文档列表进行分割处理。
     * 配置参数设置为：最大 token 数量 1000，段落重叠 token 数量 400，
     * 最小分段 token 数量 10，最大字符数限制 5000，且仅在段落边界分割。
     *
     * @param documents 需要被分割的原始文档列表
     * @return 分割处理后的文档列表，每个文档满足自定义配置的限制条件
     */
    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
        return splitter.apply(documents);
    }
}
