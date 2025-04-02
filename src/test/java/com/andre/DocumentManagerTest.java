package com.andre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;
    private DocumentManager.Document doc1;
    private DocumentManager.Document doc2;
    private DocumentManager.Document doc3;
    private Instant now;
    private Instant oneWeekAgo;
    private Instant threeDaysAgo;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
        now = Instant.now();
        oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
        threeDaysAgo = now.minus(3, ChronoUnit.DAYS);

        doc1 = DocumentManager.Document.builder()
                .title("Java Programming")
                .content("Java")
                .author(DocumentManager.Author.builder()
                        .id("author1")
                        .name("JD")
                        .build())
                .created(now)
                .build();

        doc2 = DocumentManager.Document.builder()
                .id("custom-id")
                .title("Python")
                .content("Python")
                .author(DocumentManager.Author.builder()
                        .id("author2")
                        .name("JS")
                        .build())
                .created(oneWeekAgo)
                .build();

        doc3 = DocumentManager.Document.builder()
                .title("JavaScript")
                .content("JavaScript is used in web development ... ")
                .author(DocumentManager.Author.builder()
                        .id("author1")
                        .name("JD")
                        .build())
                .created(threeDaysAgo)
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);
        documentManager.save(doc3);
    }

    @Test
    void testSaveNewDocument() {
        DocumentManager.Document newDoc = DocumentManager.Document.builder()
                .title("New Document")
                .content("This is a new document.")
                .author(DocumentManager.Author.builder()
                        .id("author3")
                        .name("JB")
                        .build())
                .build();

        DocumentManager.Document savedDoc = documentManager.save(newDoc);

        assertNotNull(savedDoc.getId());
        assertFalse(savedDoc.getId().isEmpty());

        assertNotNull(savedDoc.getCreated());

        Optional<DocumentManager.Document> retrieved = documentManager.findById(savedDoc.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(savedDoc.getTitle(), retrieved.get().getTitle());
    }

    @Test
    void testSaveExistingDocument() {
        String existingId = doc1.getId();
        Instant originalCreated = doc1.getCreated();

        DocumentManager.Document updatedDoc = DocumentManager.Document.builder()
                .id(existingId)
                .title("Updated Java")
                .content("Updated content")
                .author(DocumentManager.Author.builder()
                        .id("author1")
                        .name("JD")
                        .build())
                .build();

        DocumentManager.Document savedDoc = documentManager.save(updatedDoc);

        assertEquals(existingId, savedDoc.getId());

        assertEquals(originalCreated, savedDoc.getCreated());

        assertEquals("Updated Java", savedDoc.getTitle());
        assertEquals("Updated content", savedDoc.getContent());
    }

    @Test
    void testFindById() {
        Optional<DocumentManager.Document> found = documentManager.findById(doc2.getId());
        assertTrue(found.isPresent());
        assertEquals("Python", found.get().getTitle());

        Optional<DocumentManager.Document> notFound = documentManager.findById("non-existent-id");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testSearchByTitlePrefix() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Arrays.asList("Java"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(d -> d.getTitle().equals("JavaScript")));
    }

    @Test
    void testSearchByContent() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(Arrays.asList("web"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size());
        assertEquals("JavaScript", results.get(0).getTitle());
    }

    @Test
    void testSearchByAuthor() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(Arrays.asList("author1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(d -> d.getAuthor().getId().equals("author1")));
    }

    @Test
    void testSearchByDateRange() {
        Instant fiveDaysAgo = now.minus(5, ChronoUnit.DAYS);
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(fiveDaysAgo)
                .createdTo(oneDayAgo)
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size());
        assertEquals("JavaScript", results.getFirst().getTitle());
    }

    @Test
    void testSearchWithMultipleCriteria() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(Arrays.asList("author1"))
                .titlePrefixes(Arrays.asList("Java"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(d ->
                d.getAuthor().getId().equals("author1") &&
                        d.getTitle().startsWith("Java")));
    }

    @Test
    void testSearchWithNoMatchingResults() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Arrays.asList("C++"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchWithNullRequest() {
        List<DocumentManager.Document> results = documentManager.search(null);
        assertEquals(3, results.size());
    }

    @Test
    void testSearchWithEmptyFields() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Collections.emptyList())
                .authorIds(Collections.emptyList())
                .containsContents(Collections.emptyList())
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(3, results.size());
    }
}
