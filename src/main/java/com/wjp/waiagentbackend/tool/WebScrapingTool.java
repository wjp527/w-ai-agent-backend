package com.wjp.waiagentbackend.tool;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网络抓取工具
 * @author wjp
 */
public class WebScrapingTool {
    @Tool(description = "Scrape the content of a web page")
    public String scrapWebPage(@ToolParam(description = "URL of the web page to scape") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }

    }

}
