package social.benji.benji_backend_api.blog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;

/**
 * Creation DTO for blog posts (counterpart of BlogPostDto). Fields mirror what
 * BlogPostMapper.toEntity consumes. The author is never taken from the payload:
 * it is derived from the authenticated principal in the controller.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostDto {

    private String title;
    private String content;
    private List<String> tags;
    private List<String> categories;
    private List<String> attachmentIds;
    private BlogPostStatus blogPostStatus;
    /** Optional display-name snapshot; falls back to the masked mobile when absent. */
    private String authorName;
}
