package com.proga.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    @Autowired(required = false)
    private VectorStore vectorStore;

    /**
     * Perform Similarity Search over PgVector Store
     */
    public List<Document> searchSimilarDocuments(String query, int topK) {
        if (vectorStore == null) {
            log.warn("VectorStore bean is not active. Falling back to static dataset.");
            return Collections.emptyList();
        }
        try {
            return vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .similarityThreshold(0.65)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error executing Vector Similarity Search: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Add Document chunks to PgVector Store
     */
    public void ingestDocuments(List<Document> documents) {
        if (vectorStore == null) {
            log.warn("VectorStore is disabled. Cannot ingest documents.");
            return;
        }
        try {
            vectorStore.add(documents);
            log.info("Successfully ingested {} document chunks into PgVector Store.", documents.size());
        } catch (Exception e) {
            log.error("Failed to ingest documents into PgVector Store: {}", e.getMessage());
        }
    }
}
