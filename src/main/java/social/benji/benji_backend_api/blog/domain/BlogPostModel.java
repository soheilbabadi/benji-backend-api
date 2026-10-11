package social.benji.benji_backend_api.blog.domain;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;

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
@Document(collection = "blog_posts")
@CompoundIndex(name = "status_createdAt", def = "{'blogPostStatus': 1, 'createdAt': -1}")
@CompoundIndex(name = "author_createdAt", def = "{'authorId': 1, 'createdAt': -1}")
public class BlogPostModel {


    private String id;
    @Indexed(unique = true)
    private String slug;
    private String title;
    /** Short SEO/social preview text shown in listings without loading full content. */
    private String excerpt;
    /** Cover/hero image URL or attachment id for cards and social share previews. */
    private String coverImageUrl;
    private String content;
    @Indexed
    private String authorId;
    private String authorName;
    @Indexed
    private List<String> tags;
    @Indexed
    private List<String> categories;
    private List<String> attachmentIds;
    @Indexed
    private BlogPostStatus blogPostStatus;
    private Long views;
    private Integer commentsCount;
    private Integer likesCount;
    private Boolean featured;
    /** True once published; drives feed queries that must exclude drafts. */
    private Boolean published;
    /** When the post actually went live (differs from createdAt for scheduled posts). */
    private Instant publishedAt;
    /** Optional future date for scheduling publication. */
    private Instant scheduledFor;
    /** Estimated read time in minutes, precomputed at save to avoid runtime cost. */
    private Integer readingTimeMinutes;
    /** Soft-delete flag instead of removing documents (keeps slugs/comments recoverable). */
    private Boolean deleted;
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
