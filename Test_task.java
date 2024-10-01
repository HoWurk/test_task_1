import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    // If it should be as simple to read as possible, and not to worry about performance (faster lookup etc)
    // then I make a List instead of a Map
    // I also assume lambdas are simple to read (can be changed to longer non-lambdas if needed) and methods like removeIf are simple to read
    // (compared to findAny and then removing)
    private final List<Document> documents = new ArrayList<>();
    // In-memory id generator
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null) {
            document.setId(String.valueOf(idGenerator.getAndIncrement()));
        } else {
            documents.removeIf(doc ->
                    doc.getId().equals(document.getId()));
        }
        documents.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documents.stream()
                .filter(document -> matchesRequest(document, request))
                .collect(Collectors.toList());
    }

    // Made simpler by extracting methods
    private boolean matchesRequest(Document document, SearchRequest request) {
        if (titlePrefixesDontMatch(document, request)) return false;

        if (documentContentsDontMatch(document, request)) return false;

        if (authorIdsDontMatch(document, request)) return false;

        if (isNotCreatedAfter(document, request)) return false;

        if (isNotCreatedBefore(document, request)) return false;

        return true;
    }

    // If fields can't be null, this can be made more simple/readable
    // This can cut some lines but make it a little more complex to understand
    private static boolean titlePrefixesDontMatch(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matchTitle = request.getTitlePrefixes().stream()
                    .anyMatch(prefix ->
                            document.getTitle().startsWith(prefix));
            if (!matchTitle) return true;
        }
        return false;
    }

    private static boolean documentContentsDontMatch(Document document, SearchRequest request) {
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matchContent = request.getContainsContents().stream()
                    .anyMatch(document.getContent()::contains);
            if (!matchContent) return true;
        }
        return false;
    }

    private static boolean authorIdsDontMatch(Document document, SearchRequest request) {
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            boolean matchAuthorIds = request.getAuthorIds()
                    .contains(document.getAuthor().getId());
            if (!matchAuthorIds) return true;
        }
        return false;
    }

    private static boolean isNotCreatedAfter(Document document, SearchRequest request) {
        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return true;
        }
        return false;
    }

    private static boolean isNotCreatedBefore(Document document, SearchRequest request) {
        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return true;
        }
        return false;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return documents.stream().filter(doc ->
                        doc.getId().equals(id))
                .findFirst();
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