package com.xia.agentprogram.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    public VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();

        // 手动调整切分文档,很难把握合适值,容易破坏语义完整性
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
//        simpleVectorStore.add(splitDocuments);
        // 使用关键词增强文档
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
