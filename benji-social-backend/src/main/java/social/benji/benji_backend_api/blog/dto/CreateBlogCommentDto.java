package social.benji.benji_backend_api.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Creation DTO for blog post comments. Author identity fields are intentionally
 * absent: the author is taken from the authenticated principal in the controller.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogCommentDto {

    private String blogPostId;
    private String content;
    /** Immediate parent comment id; null for top-level comments. */
    private String parentId;
    /** Client-generated id for idempotent submission. */
    private String clientRequestId;
}
