package com.xia.agentprogram.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
@Slf4j
class LoveAppDocumentLoaderTest {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        log.info("Documents: {}", documents);
    }
}