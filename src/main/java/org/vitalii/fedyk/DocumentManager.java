package org.vitalii.fedyk;

import lombok.*;

import java.time.Instant;
import java.util.*;
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
    private final Set<Document> documents = new HashSet<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(@NonNull final Document document) {
        if (Objects.isNull(document.getId())) {
            document.setId(generateId());
        }
        Optional<Document> savedDocument = findById(document.getId());
        if (savedDocument.isPresent()) {
            return updatePartially(document);
        }
        documents.add(document);
        return document;
    }

    private Document updatePartially(final Document document) {
        document.setTitle(document.getTitle());
        document.setContent(document.getContent());
        return document;
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(@NonNull final SearchRequest request) {
        return documents.stream()
                .filter(document -> checkCriteria(document, request))
                .collect(Collectors.toList());
    }

    private boolean checkCriteria(final Document document, final SearchRequest request) {
        return matchesTitlePrefix(document, request)
               && matchesContent(document, request)
               && matchesAuthor(document, request)
               && matchesCreatedFrom(document, request)
               && matchesCreatedTo(document, request);

    }

    private boolean matchesTitlePrefix(final Document document, final SearchRequest request) {
        if (Objects.nonNull(request.getTitlePrefixes()) && Objects.nonNull(document.getTitle())) {
            return request.getTitlePrefixes().stream().anyMatch(document.getTitle()::startsWith);
        }
        return true;
    }

    private boolean matchesContent(final Document document, final SearchRequest request) {
        if (Objects.nonNull(request.getContainsContents()) && Objects.nonNull(document.getContent())) {
            return request.getContainsContents().stream().anyMatch(document.getContent()::contains);
        }
        return true;
    }

    private boolean matchesAuthor(final Document document, final SearchRequest request) {
        if (Objects.nonNull(request.getAuthorIds())) {
            return request.getAuthorIds().stream()
                    .anyMatch(document.getAuthor().getId()::equals);
        }
        return true;
    }

    private boolean matchesCreatedFrom(final Document document, final SearchRequest request) {
        if (Objects.nonNull(request.getCreatedFrom())) {
            return document.getCreated().isAfter(request.getCreatedFrom());
        }
        return true;
    }


    private boolean matchesCreatedTo(final Document document, final SearchRequest request) {
        if (Objects.nonNull(request.getCreatedTo())) {
            return document.getCreated().isBefore(request.getCreatedTo());
        }
        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(@NonNull final String id) {
        return documents.stream()
                .filter(document -> document.getId().equals(id))
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
    @EqualsAndHashCode(of = "id")
    public static class Document {
        @Setter(value = AccessLevel.PRIVATE)
        private String id;
        private String title;
        private String content;
        @Setter(value = AccessLevel.PRIVATE)
        private Author author;
        @Setter(value = AccessLevel.PRIVATE)
        private Instant created = Instant.now();

        @Builder
        public Document(String id, String title, String content, @NonNull Author author) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
        }
    }

    @Data
    @EqualsAndHashCode(of = "id")
    public static class Author {
        @Setter(value = AccessLevel.PRIVATE)
        private String id = generateId(); //Autogenerated id. Cannot be changed after creation
        private String name;

        @Builder
        public Author(@NonNull String name) {
            this.name = name;
        }
    }
}