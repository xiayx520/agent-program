package com.xia.agentprogram.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    @Resource
    private ResourcePatternResolver resourcePatternResolver;


    /**
     * 加载Markdown文档
     * @return
     */
    public List<Document> loadMarkdowns() {
        log.info("开始加载Markdown文档");
        List<Document> documents = new ArrayList<>();
        try {
            org.springframework.core.io.Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");

            for (org.springframework.core.io.Resource resource : resources) {
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", Objects.requireNonNull(resource.getFilename()))
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.get());
            }
        } catch (IOException e) {

            log.error("Markdown 文档加载失败", e);
        }
        return documents;
    }
}
