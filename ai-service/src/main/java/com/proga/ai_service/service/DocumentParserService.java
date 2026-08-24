package com.proga.ai_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParserService {

    private final VectorStoreService vectorStoreService;
    private final Tika tika = new Tika();

    public DocumentParseResponse parseAndIngestDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để rỗng");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "Uploaded_Document";
        log.info("Parsing document: {}, size: {} bytes", originalFilename, file.getSize());

        String extractedText = "";

        String fileNameLower = originalFilename.toLowerCase();
        
        // Strategy 1: Direct UTF-8 Reading for plain text / markdown files
        if (fileNameLower.endsWith(".txt") || fileNameLower.endsWith(".md") || fileNameLower.endsWith(".json") || fileNameLower.endsWith(".csv")) {
            try {
                extractedText = new String(file.getBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("Direct UTF-8 read failed for {}, trying Tika: {}", originalFilename, e.getMessage());
            }
        }

        // Strategy 2: Apache Tika Parser Engine for PDF, DOCX, DOC, PPTX
        if (extractedText.isBlank()) {
            try (InputStream inputStream = file.getInputStream()) {
                extractedText = tika.parseToString(inputStream);
            } catch (Throwable t) {
                log.warn("Apache Tika parseToString failed for file {}: {}. Trying byte stream fallback.", originalFilename, t.getMessage());
                try {
                    String rawStr = new String(file.getBytes(), StandardCharsets.UTF_8);
                    // Filter printable characters
                    extractedText = rawStr.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\s]", " ");
                } catch (Exception ignored) {
                    extractedText = "Không thể bóc tách nội dung từ file " + originalFilename;
                }
            }
        }

        if (extractedText == null || extractedText.isBlank()) {
            extractedText = "Tài liệu " + originalFilename + " không chứa nội dung văn bản bóc tách được.";
        }

        // Clean multiple newlines and spaces
        String cleanedText = extractedText.replaceAll("\\r\\n|\\r", "\n").replaceAll("\n{3,}", "\n\n").trim();
        
        // Chunking for Vector Store (1,000 chars / chunk)
        List<Document> docChunks = createChunks(cleanedText, originalFilename);
        
        // Ingest into Vector Store if available
        try {
            vectorStoreService.ingestDocuments(docChunks);
        } catch (Exception e) {
            log.warn("Vector Store ingestion warning: {}", e.getMessage());
        }

        String previewText = cleanedText.length() > 3000 ? cleanedText.substring(0, 3000) + "...\n[Đã bóc tách thành công toàn bộ vào Vector Store]" : cleanedText;

        return DocumentParseResponse.builder()
                .fileName(originalFilename)
                .fileSize(file.getSize())
                .extractedText(previewText)
                .chunkCount(docChunks.size())
                .build();
    }

    private List<Document> createChunks(String text, String fileName) {
        List<Document> chunks = new ArrayList<>();
        int chunkSize = 1000; // characters
        int overlap = 100;
        
        int length = text.length();
        int start = 0;
        int chunkIdx = 1;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunkContent = text.substring(start, end);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("file_name", fileName);
            metadata.put("chunk_index", chunkIdx++);
            
            chunks.add(new Document(chunkContent, metadata));
            
            start += chunkSize - overlap;
            if (start >= length) break;
        }

        return chunks;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentParseResponse {
        private String fileName;
        private long fileSize;
        private String extractedText;
        private int chunkCount;
    }
}
