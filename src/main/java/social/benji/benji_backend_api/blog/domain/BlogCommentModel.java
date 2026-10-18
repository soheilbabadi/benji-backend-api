package social.benji.benji_backend_api.blog.domain;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import social.benji.benji_backend_api.blog.enums.CommentStatus;

/**
 * Mongo document representing a single comment on a blog post. Supports
 * threaded (nested) replies via parentId/rootCommentId, moderation via
 * {@link CommentStatus}, and denormalized counters for cheap listing.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blog_comments")
@CompoundIndex(name = "post_createdAt", def = "{'blogPostId': 1, 'createdAt': -1}")
@CompoundIndex(name = "post_status_createdAt", def = "{'blogPostId': 1, 'status': 1, 'createdAt': -1}")
public class BlogCommentModel {

    private String id;
    /** Post this comment belongs to (indexed via compound index above). */
    @Indexed
    private String blogPostId;
    /** Author user id; null for anonymous guests. */
    @Indexed
    private String authorId;
    /** Display name snapshot so listings don't join the user collection. */
    private String authorName;
    /** Optional email/avatar for anonymous or display purposes. */
    private String authorEmail;
    private String authorAvatarUrl;
    /** The comment text itself. */
    private String content;
    /** Immediate parent comment id; null for top-level comments. */
    private String parentId;
    /** Top-level ancestor of a thread, so whole threads load in one query. */
    private String rootCommentId;
    /** True when this comment starts a thread (denormalized for fast queries). */
    private Boolean topLevel;
    /** Depth of nesting, useful for rendering/thread caps. */
    private Integer depth;
    /** Number of direct replies, cached to avoid counting on read. */
    private Integer repliesCount = 0;
    /** Number of likes for the comment, updated on each like/unlike action. */
    private Integer likesCount = 0;
    /** Moderation state; only APPROVED comments show publicly. */
    @Indexed
    private CommentStatus status;
    /** Reason recorded by moderator/auto-filter when marked SPAM/DELETED. */
    private String moderationNote;
    /** Id of the moderator who changed status, if any. */
    private String moderatedBy;
    private Instant moderatedAt;
    /** Soft delete keeps the thread structure intact for children. */
    private Boolean deleted;
    /** Content edited after posting. */
    private Boolean edited;
    /** Client-generated id for idempotent comment submission. */
    @Indexed
    private String clientRequestId;
    /** IP address and user agent captured for abuse review. */
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
