package social.benji.benji_backend_api.blog.domain;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plain in-memory model matching the field set BlogPostMapper maps. The Mongo
 * document (blog.BlogPostModel) has a different shape (Set-based ids, no
 * views/tags-as-list), so it cannot back the mapper without inventing behavior.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostModel {

    public enum Status {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        ARCHIVED
    }

    private String id;
    private String title;
    private String slug;
    private String content;
    private String authorId;
    private String authorName;
    private List<String> tags;
    private List<String> categories;
    private List<String> attachmentIds;
    private Status status;
    private Long views;
    private Integer commentsCount;
    private Boolean featured;
    private Instant createdAt;
    private Instant updatedAt;
}
