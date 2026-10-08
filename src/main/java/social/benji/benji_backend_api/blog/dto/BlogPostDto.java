package social.benji.benji_backend_api.blog.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;

/**
 * Read DTO for a blog post. Kept minimal: only fields BlogPostMapper actually
 * maps. The mapper referenced classes that were never committed, which broke
 * the whole build; this restores compilability without inventing new behavior.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDto {

    private String id;
    private String title;
    private String slug;
    private String content;
    private String authorId;
    private String authorName;
    private List<String> tags;
    private List<String> categories;
    private List<String> attachmentIds;
    private BlogPostModel.Status status;
    private Long views;
    private Instant createdAt;
    private Instant updatedAt;
}
