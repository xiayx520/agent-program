package com.xia.agentprogram.rag;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;


@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    /** DashScope 向量化接口单次请求最多支持 10 条文本，超过会报 "batch size is invalid, it should not be larger than 10." */
    private static final int DASHSCOPE_EMBEDDING_BATCH_SIZE = 10;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .batchingStrategy(documents -> {
                    List<List<Document>> batches = new ArrayList<>();
                    for (int i = 0; i < documents.size(); i += DASHSCOPE_EMBEDDING_BATCH_SIZE) {
                        batches.add(documents.subList(i, Math.min(i + DASHSCOPE_EMBEDDING_BATCH_SIZE, documents.size())));
                    }
                    return batches;
                })
                .build();

        importDocumentsIdempotently(jdbcTemplate, vectorStore);
        return vectorStore;
    }

    /**
     * 幂等导入文档：为每篇文档生成基于内容哈希的固定 ID，
     * 与表中已有 ID 比对，只写入不存在的新文档，避免重启时重复写入
     */
    private void importDocumentsIdempotently(JdbcTemplate jdbcTemplate, VectorStore vectorStore) {
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        if (documents.isEmpty()) {
            return;
        }
        // 用内容哈希生成稳定 ID，相同内容每次启动得到的 ID 一致
        List<Document> stableIdDocuments = documents.stream()
                .map(doc -> Document.builder()
                        .id(UUID.nameUUIDFromBytes(doc.getText().getBytes(StandardCharsets.UTF_8)).toString())
                        .text(doc.getText())
                        .metadata(doc.getMetadata())
                        .build())
                .toList();

        Set<String> existingIds = Set.copyOf(jdbcTemplate.queryForList(
                "SELECT id::text FROM public.vector_store", String.class));

        List<Document> newDocuments = stableIdDocuments.stream()
                .filter(doc -> !existingIds.contains(doc.getId()))
                .toList();
        if (newDocuments.isEmpty()) {
            log.info("向量库文档已是最新，跳过导入，共 {} 篇", stableIdDocuments.size());
            return;
        }
        vectorStore.add(newDocuments);
        log.info("文档导入完成：新增 {} 篇，跳过已存在 {} 篇", newDocuments.size(), stableIdDocuments.size() - newDocuments.size());
    }

}
