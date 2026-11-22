package com.xia.agentprogram.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebsite() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.baidu.com";
        String result = webScrapingTool.scrapeWebsite(url);
        System.out.println(result);
    }
}