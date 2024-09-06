import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

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
        if (Objects.isNull(document.getId())) {
            while (documents.containsKey(String.valueOf(id))) { //finding the smallest free key
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
        return request != null ? documents.values().stream()
                .filter(doc -> matchesSearchCriteria(request.getTitlePrefixes(), prefix -> doc.getTitle().startsWith(prefix)))
                .filter(doc -> matchesSearchCriteria(request.getContainsContents(), content -> doc.getContent().contains(content)))
                .filter(doc -> matchesSearchCriteria(request.getAuthorIds(), authorId -> doc.getAuthor().getId().equals(authorId)))
                .filter(doc -> matchesSingleSearchCriteria(request.getCreatedFrom(), createdFrom -> doc.getCreated().isAfter(createdFrom)))
                .filter(doc -> matchesSingleSearchCriteria(request.getCreatedTo(), createdTo -> doc.getCreated().isBefore(createdTo)))
                .toList() : Collections.emptyList();
    }

    private <T> boolean matchesSearchCriteria(List<T> values, Predicate<T> predicate) { //for List criteria
        return Objects.isNull(values) || values.stream().anyMatch(predicate);
    }

    private <T> boolean matchesSingleSearchCriteria(T value, Predicate<T> predicate) { //for single criteria
        return Objects.isNull(value) || predicate.test(value);
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