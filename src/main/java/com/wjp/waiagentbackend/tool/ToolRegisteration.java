package com.wjp.waiagentbackend.tool;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 集中注册工具类
 */
@Configuration
public class ToolRegisteration {
    // 联网搜索API_KEY
    @Value("${search-api.api-key}")
    private String searchApiKey;


    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Resource
    private EmailTool emailTool;


    // Spring AI 怎么识别工具
    @Bean
    public ToolCallback[] allTools() {
        // 文件读/写工具
        FileOperationTool fileOperationTool = new FileOperationTool();
        // 联网搜索
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        // 网页抓取
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        // 资源下载
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        // 终端操作
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        // PDF生成
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        // 发送邮箱工具
//        EmailTool emailTool = new EmailTool(javaMailSender, from);

        // 转换成 Spring AI能够用的工具【使用 @Tool和@ToolParam 注解】
        return ToolCallbacks.from(
            fileOperationTool,
            webSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool,
            emailTool
        );
    }

}
