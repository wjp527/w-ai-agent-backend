package com.wjp.waiagentbackend.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师应用文档加载器 [可以打标签]
 */
@Component
@Slf4j

public class LoveAppDocumentLoader {
    // 注入一个ResourcePatternResolver对象，用于加载资源文件
    private final ResourcePatternResolver resourcePatternResolver;

    // 构造函数，注入一个ResourcePatternResolver对象
    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }


    /**
     * 加载Markdown文件
     * 该方法从类路径下的document目录中加载所有Markdown文件，并将它们的内容解析为Document对象
     *
     * @return 包含所有解析后的Document对象的列表
     */
    public List<Document> loadMarkdowns() {
        // 创建一个列表，用于存储所有解析后的Document对象
        List<Document> allDocuments = new ArrayList<>();
        // 加载多篇md文件
        try {
            // 获取类路径下document目录中的所有Markdown文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            
            // 遍历每个Markdown文件
            for (Resource resource : resources) {
                // 获取文件名【后续进行添加标识】
                String filename = resource.getFilename();
                // 获取文档的标签
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
//                String status = filename.substring(filename.indexOf("-") + 1, filename.indexOf("篇"));
                // 创建一个MarkdownDocumentReaderConfig对象，并设置一些配置选项
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        // 是否包含
                        .withHorizontalRuleCreateDocument(true)
                        // 是否包含代码块
                        .withIncludeCodeBlock(false)
                        // 是否包含引用块
                        .withIncludeBlockquote(false)
                        // 添加元数据(打标签)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        //
                        .build();

                // 创建一个MarkdownDocumentReader对象，用于读取和解析Markdown文件
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                // 将解析后的Document对象添加到列表中
                allDocuments.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
            // 如果文件加载失败，记录错误日志
            log.error("Markdown 文档加载失败.", e);
        }

        // 返回包含所有解析后的Document对象的列表
        return allDocuments;
    }

}
