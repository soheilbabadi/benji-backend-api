package social.benji.benji_backend_api.blog.service;

import org.springframework.data.domain.Page;
import social.benji.benji_backend_api.blog.dto.BlogPostDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogPostDto;
import social.benji.benji_backend_api.blog.dto.UpdateBlogPostDto;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;

import java.util.List;

public interface BlogPostService {

    /** Paginated list of posts with the given status, newest first. */
    Page<BlogPostDto> list(BlogPostStatus status, int page, int size);
    Page<BlogPostDto> list(int page, int size);

    BlogPostDto findById(String id);

    BlogPostDto findBySlug(String slug);

    List<BlogPostDto> findByAuthorId(String authorId);

    BlogPostDto create(CreateBlogPostDto dto);

    BlogPostDto update(String id, UpdateBlogPostDto dto);

    void deleteById(String id);
}
