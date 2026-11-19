package com.xia.agentprogram.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建一个自定义的文档查询器RAG顾问，用于根据状态过滤文档
 */

public class LoveAppRagCustomAdvisorFactory {
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.6)// 设置相似度阈值
                .topK(3)// 设置返回的文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                // 设置文档检索器，查询时从向量库中召回相关文档，作为上下文增强用户问题
                .documentRetriever(documentRetriever)
                // 上下文查询增强器
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }

}
