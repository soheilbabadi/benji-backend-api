package social.benji.benji_backend_api.blog.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social.benji.benji_backend_api.blog.enums.CommentStatus;

/**
 * Read DTO for a blog post comment.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCommentDto {

    private String id;
    private String blogPostId;
    private String authorId;
    private String authorName;
    private String content;
    private String parentId;
    private String rootCommentId;
    private Boolean topLevel;
    private Integer depth;
    private Integer repliesCount;
    private Integer likesCount;
    private CommentStatus status;
    private Boolean edited;
    private Instant createdAt;
    private Instant updatedAt;
}
