package social.benji.benji_backend_api.blog.service;

import org.springframework.data.domain.Page;
import social.benji.benji_backend_api.blog.dto.BlogPostDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogPostDto;
import social.benji.benji_backend_api.blog.dto.UpdateBlogPostDto;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.List;

public interface BlogPostService {

    /** Paginated list of posts with the given status, newest first. */
    Page<BlogPostDto> list(BlogPostStatus status, int page, int size);
    Page<BlogPostDto> list(int page, int size);

    BlogPostDto findById(String id);

    BlogPostDto findBySlug(String slug);

    List<BlogPostDto> findByAuthorId(String authorId);

    /** Creates a post owned by {@code creator}; only the caller may later update/delete it (unless ADMIN). */
    BlogPostDto create(CreateBlogPostDto dto, UserPrincipal creator);

    /** Updates a post; rejected unless the caller owns it or has ROLE_ADMIN. */
    BlogPostDto update(String id, UpdateBlogPostDto dto, UserPrincipal editor);

    /** Deletes a post and its comments; rejected unless the caller owns it or has ROLE_ADMIN. */
    void deleteById(String id, UserPrincipal requester);
}
