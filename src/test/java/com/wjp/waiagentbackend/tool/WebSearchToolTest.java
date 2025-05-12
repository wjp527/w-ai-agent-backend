package com.wjp.waiagentbackend.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest

class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;
    @Test
    void baiduSearch() throws Exception {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        // 关键词要包含英文[最好全英文，毕竟是国外的接口网站！！！]
//        String query = "程序员鱼皮编程导航 codefather.cn";
//        String query = "bilibili";
        String query = "The history of the Chinese table tennis team";
        String result = webSearchTool.searchWeb(query);
        assertNotNull(result);
    }
}