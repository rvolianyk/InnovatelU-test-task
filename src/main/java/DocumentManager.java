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

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (Objects.isNull(document.getId())) {
            document.setId(uniqueId());
        }
        Document target = documents.get(document.getId());
        if (Objects.nonNull(target)) {
            document = update(target, document);
        }
        documents.put(document.getId(), document);
        return document;
    }

    private String uniqueId() {
        Integer maxId = documents.keySet().stream().map(Integer::valueOf).max(Integer::compareTo).orElse(-1);
        return String.valueOf(++maxId);
    }

    private Document update(Document target, Document updates) {
        target.setAuthor(updates.getAuthor());
        target.setContent(updates.getContent());
        target.setTitle(updates.getTitle());
        return target;
    }

    /**
     * Implementation of this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (Objects.isNull(request)) {
            return Collections.emptyList();
        }
        return documents.values().stream()
                .filter(doc -> isTitleStartWith(doc, request))
                .filter(doc -> containsContent(doc, request))
                .filter(doc -> matchAuthorIds(doc, request))
                .filter(doc -> inDateRange(doc, request))
                .toList();
    }

    private boolean isTitleStartWith(Document doc, SearchRequest request) {
        return matchesSearchCriteria(request.getTitlePrefixes(), prefix -> doc.getTitle().toLowerCase().startsWith(prefix.toLowerCase()));
    }

    private boolean containsContent(Document doc, SearchRequest request) {
        return matchesSearchCriteria(request.getContainsContents(), content -> doc.getContent().toLowerCase().contains(content.toLowerCase()));
    }

    private boolean matchAuthorIds(Document doc, SearchRequest request) {
        return matchesSearchCriteria(request.getAuthorIds(), authorId -> doc.getAuthor().getId().equals(authorId));
    }

    private <T> boolean matchesSearchCriteria(List<T> values, Predicate<T> predicate) {
        return Objects.isNull(values) || values.stream().anyMatch(predicate);
    }

    private boolean inDateRange(Document doc, SearchRequest request) {
        return matchesSingleSearchCriteria(request.getCreatedFrom(), createdFrom -> doc.getCreated().isAfter(createdFrom))
                && matchesSingleSearchCriteria(request.getCreatedTo(), createdTo -> doc.getCreated().isBefore(createdTo));
    }

    private <T> boolean matchesSingleSearchCriteria(T value, Predicate<T> predicate) {
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