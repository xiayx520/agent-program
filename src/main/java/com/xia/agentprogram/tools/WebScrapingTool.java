package com.xia.agentprogram.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

@Slf4j
public class WebScrapingTool {

    /**
     * 请求超时时间（毫秒），避免长时间阻塞 Agent 调用
     */
    private static final int TIMEOUT_MILLIS = 10000;

    /**
     * 浏览器 User-Agent，避免被网站反爬机制拦截返回 403
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    /**
     * 返回给模型的最大文本长度，防止超大页面撑爆上下文窗口
     */
    private static final int MAX_TEXT_LENGTH = 500;

    @Tool(description = "Scrape content from a web page")
    public String scrapeWebsite(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT_MILLIS)
                    .userAgent(USER_AGENT)
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                    .get();
            // 移除脚本、样式等与正文无关的元素，减少无关内容
            doc.select("script, style, noscript, iframe").remove();
            String html = doc.html();
            // 超长文本截断，只保留前 MAX_TEXT_LENGTH 个字符
            if (html.length() > MAX_TEXT_LENGTH) {
                html = html.substring(0, MAX_TEXT_LENGTH);
            }
            return html;
        } catch (HttpStatusException e) {
            // 目标站点返回非 2xx 状态码，需放在 IOException 之前捕获（它是 IOException 的子类）
            log.warn("抓取网页被服务器拒绝, status: {}, url: {}", e.getStatusCode(), url);
            if (e.getStatusCode() == 403) {
                // 返回明确的降级提示，引导模型改用搜索工具，而不是把原始异常信息丢给模型
                return "无法抓取该网页（HTTP 403）：该网站启用了严格的反爬虫机制或要求登录。";
            }
            return "Error scraping web page: HTTP " + e.getStatusCode();
        } catch (IOException e) {
            log.error("抓取网页内容失败, url: {}", url, e);
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
