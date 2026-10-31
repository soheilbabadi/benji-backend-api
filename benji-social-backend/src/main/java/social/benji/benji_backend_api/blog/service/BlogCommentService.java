package social.benji.benji_backend_api.blog.service;

import org.springframework.data.domain.Page;
import social.benji.benji_backend_api.blog.dto.BlogCommentDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogCommentDto;
import social.benji.benji_backend_api.blog.enums.CommentStatus;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.List;

public interface BlogCommentService {

    /** Paginated approved comments for a post, newest first. */
    Page<BlogCommentDto> listByPost(String blogPostId, int page, int size);

    /** All comments of a post in creation order (moderation view). */
    List<BlogCommentDto> findAllByPost(String blogPostId);

    /** Whole thread (root + replies) for a top-level comment. */
    List<BlogCommentDto> findThread(String rootCommentId);

    BlogCommentDto findById(String id);

    /** Creates a comment authored by {@code author}; payload identity fields are ignored. */
    BlogCommentDto create(CreateBlogCommentDto dto, UserPrincipal author);

    BlogCommentDto like(String id);

    BlogCommentDto unlike(String id);

    /** Moderation is restricted to ROLE_ADMIN. */
    void moderate(String id, CommentStatus status, UserPrincipal moderator, String note);

    /** Deletes a comment; rejected unless the caller authored it or has ROLE_ADMIN. */
    void deleteById(String id, UserPrincipal requester);
}
