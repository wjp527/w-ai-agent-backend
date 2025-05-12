package com.wjp.waiagentbackend.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://java2ai.com/docs/1.0.0-M6.1/integrations/tools/";
        String result = webScrapingTool.scrapWebPage(url);
        assertNotNull(result);
    }
}