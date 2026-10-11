package social.benji.benji_backend_api.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.blog.domain.BlogCommentModel;
import social.benji.benji_backend_api.blog.enums.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface BlogCommentRepository extends MongoRepository<BlogCommentModel, String> {

    Page<BlogCommentModel> findByBlogPostIdAndStatusAndDeletedFalse(String blogPostId, CommentStatus status, Pageable pageable);

    List<BlogCommentModel> findByBlogPostIdAndDeletedFalse(String blogPostId);

    List<BlogCommentModel> findByRootCommentIdAndDeletedFalse(String rootCommentId);

    Optional<BlogCommentModel> findByClientRequestId(String clientRequestId);

    long countByBlogPostIdAndStatusAndDeletedFalse(String blogPostId, CommentStatus status);
}
