import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> documents = new HashMap<>();
    private int id = 1;
    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            while (documents.containsKey(String.valueOf(id))) {
                id++;
            }
            document.setId(String.valueOf(id++));
        }
        documents.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation of this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) return Collections.emptyList();

        return documents.values().stream()
                .filter(doc -> request.getTitlePrefixes() == null ||
                        request.getTitlePrefixes().stream().anyMatch(prefix -> doc.getTitle().startsWith(prefix)))
                .filter(doc -> request.getContainsContents() == null ||
                        request.getContainsContents().stream().anyMatch(content -> doc.getContent().contains(content)))
                .filter(doc -> request.getAuthorIds() == null ||
                        request.getAuthorIds().contains(doc.getAuthor().getId()))
                .filter(doc -> request.getCreatedFrom() == null ||
                        doc.getCreated().isAfter(request.getCreatedFrom()))
                .filter(doc -> request.getCreatedTo() == null ||
                        doc.getCreated().isBefore(request.getCreatedTo()))
                .collect(Collectors.toList());
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