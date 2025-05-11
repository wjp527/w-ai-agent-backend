package com.wjp.waiagentbackend.rag;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取 本地的Word文档 [不能对他打标签]
 * @author wjp
 */
@Component
@Slf4j
public class MyTikaDocumentReader {

    private final ResourcePatternResolver resourcePatternResolver;

    public MyTikaDocumentReader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载 Word 文档
     * 该方法从类路径下的 document 目录中加载所有 Word 文档，并将它们的内容解析为 Document 对象
     *
     * @return 包含所有解析后的 Document 对象的列表
     */
    public List<Document> loadWordDocuments() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 获取类路径下 document 目录中的所有 .docx 文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.docx");

            for (Resource resource : resources) {
                // 使用 Tika 解析 Word 文档内容
                Tika tika = new Tika();
                try (InputStream inputStream = resource.getInputStream()) {
                    String content = tika.parseToString(inputStream);
                    allDocuments.add(new Document(content));
                } catch (TikaException e) {
                    log.error("Word解析错误: " + e);
                }
            }
        } catch (IOException e) {
            log.error("Word 文档加载失败.", e);
        }

        return allDocuments;
    }
}
