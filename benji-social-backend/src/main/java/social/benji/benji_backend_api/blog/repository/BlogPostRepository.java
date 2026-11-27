package social.benji.benji_backend_api.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends MongoRepository<BlogPostModel, String> {

    Optional<BlogPostModel> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<BlogPostModel> findByBlogPostStatus(BlogPostStatus status, Pageable pageable);

    List<BlogPostModel> findByAuthorId(String authorId);

    Page<BlogPostModel> findByTagsContaining(String tag, Pageable pageable);

    Page<BlogPostModel> findByCategoriesContaining(String category, Pageable pageable);
}
