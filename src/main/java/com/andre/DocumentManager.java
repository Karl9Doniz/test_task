package com.andre;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documents = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());

            if (document.getCreated() == null) {
                document.setCreated(Instant.now());
            }
        } else {
            Document existingDoc = documents.get(document.getId());
            if (existingDoc != null && document.getCreated() == null) {
                document.setCreated(existingDoc.getCreated());
            }
        }

        documents.put(document.getId(), document);

        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> matchedDocuments = new ArrayList<>();

        // if no filters provided for search, return all
        if (request == null) return new ArrayList<>(documents.values());

        for (Document document : documents.values()) {
            boolean matched = true;

            // check for titles
            if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
                boolean titleMatches = false;
                for (String prefix : request.getTitlePrefixes()) {
                    if (document.getTitle() != null && document.getTitle().startsWith(prefix)) {
                        titleMatches = true;
                        break;
                    }
                }

                matched = titleMatches;
            }


            // check for content
            if (matched && request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
                boolean contentMatches = false;
                for (String content : request.getContainsContents()) {
                    if (document.getContent() != null && document.getContent().contains(content)) {
                        contentMatches = true;
                        break;
                    }
                }
                matched = contentMatches;
            }

            // check author ids
            if (matched && request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
                matched = document.getAuthor() != null && request.getAuthorIds().contains(document.getAuthor().getId());
            }

            // Check creation date range
            if (matched && request.getCreatedFrom() != null && document.getCreated() != null) {
                matched = matched && (document.getCreated().equals(request.getCreatedFrom()) ||
                        document.getCreated().isAfter(request.getCreatedFrom()));
            }

            if (matched && request.getCreatedTo() != null && document.getCreated() != null) {
                matched = matched && (document.getCreated().equals(request.getCreatedTo()) ||
                        document.getCreated().isBefore(request.getCreatedTo()));
            }

            if (matched) {
                matchedDocuments.add(document);
            }
        }
        return matchedDocuments;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return Optional.ofNullable(documents.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}