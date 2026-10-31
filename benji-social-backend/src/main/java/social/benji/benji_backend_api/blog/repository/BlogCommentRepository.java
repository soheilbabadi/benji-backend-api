package social.benji.benji_backend_api.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.blog.domain.BlogCommentModel;
import social.benji.benji_backend_api.blog.enums.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface BlogCommentRepository extends MongoRepository<BlogCommentModel, String> {

    Page<BlogCommentModel> findByBlogPostIdAndStatus(String blogPostId, CommentStatus status, Pageable pageable);

    List<BlogCommentModel> findByBlogPostId(String blogPostId);

    List<BlogCommentModel> findByRootCommentId(String rootCommentId);

    Optional<BlogCommentModel> findByClientRequestId(String clientRequestId);

    long countByBlogPostIdAndStatus(String blogPostId, CommentStatus status);
}
