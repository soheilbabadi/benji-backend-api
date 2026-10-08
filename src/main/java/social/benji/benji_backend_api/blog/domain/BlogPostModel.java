package social.benji.benji_backend_api.blog.domain;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class BlogPostModel {


    private String id;
    private String title;
    private String slug;
    private String content;
    private String authorId;
    private String authorName;
    private List<String> tags;
    private List<String> categories;
    private List<String> attachmentIds;
    private BlogPostStatus blogPostStatus;
    private Long views;
    private Integer commentsCount;
    private Boolean featured;
    private Instant createdAt;
    private Instant updatedAt;
}
