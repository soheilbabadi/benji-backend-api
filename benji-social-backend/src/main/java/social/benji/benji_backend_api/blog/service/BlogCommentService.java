package social.benji.benji_backend_api.blog.service;

import org.springframework.data.domain.Page;
import social.benji.benji_backend_api.blog.dto.BlogCommentDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogCommentDto;
import social.benji.benji_backend_api.blog.enums.CommentStatus;

import java.util.List;

public interface BlogCommentService {

    /** Paginated approved comments for a post, newest first. */
    Page<BlogCommentDto> listByPost(String blogPostId, int page, int size);

    /** All comments of a post in creation order (moderation view). */
    List<BlogCommentDto> findAllByPost(String blogPostId);

    /** Whole thread (root + replies) for a top-level comment. */
    List<BlogCommentDto> findThread(String rootCommentId);

    BlogCommentDto findById(String id);

    BlogCommentDto create(CreateBlogCommentDto dto);

    BlogCommentDto like(String id);

    BlogCommentDto unlike(String id);

    void moderate(String id, CommentStatus status, String moderatorId, String note);

    void deleteById(String id);
}
