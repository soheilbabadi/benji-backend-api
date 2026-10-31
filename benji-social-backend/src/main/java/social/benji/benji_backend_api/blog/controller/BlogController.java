package social.benji.benji_backend_api.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import social.benji.benji_backend_api.blog.dto.BlogCommentDto;
import social.benji.benji_backend_api.blog.dto.BlogPostDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogCommentDto;
import social.benji.benji_backend_api.blog.dto.CreateBlogPostDto;
import social.benji.benji_backend_api.blog.dto.UpdateBlogPostDto;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;
import social.benji.benji_backend_api.blog.enums.CommentStatus;
import social.benji.benji_backend_api.blog.service.BlogCommentService;
import social.benji.benji_backend_api.blog.service.BlogPostService;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.util.List;

/**
 * Blog REST API. GET endpoints are public (see SecurityConfig); every write
 * endpoint requires a valid Bearer token and enforces author ownership (or
 * ROLE_ADMIN) inside the service layer. The caller identity always comes from
 * the verified JWT principal, never from the request body.
 */
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogPostService blogPostService;
    private final BlogCommentService blogCommentService;

    @GetMapping("/posts")
    public ResponseEntity<Page<BlogPostDto>> listPosts(
            @RequestParam(required = false) BlogPostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (status == null) {
            return ResponseEntity.ok(blogPostService.list(page, size));
        }
        return ResponseEntity.ok(blogPostService.list(status, page, size));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BlogPostDto> getPostById(@PathVariable String id) {
        return ResponseEntity.ok(blogPostService.findById(id));
    }

    @GetMapping("/posts/slug/{slug}")
    public ResponseEntity<BlogPostDto> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogPostService.findBySlug(slug));
    }

    @GetMapping("/posts/author/{authorId}")
    public ResponseEntity<List<BlogPostDto>> getPostsByAuthor(@PathVariable String authorId) {
        return ResponseEntity.ok(blogPostService.findByAuthorId(authorId));
    }

    @PostMapping("/posts")
    public ResponseEntity<BlogPostDto> createPost(@Valid @RequestBody CreateBlogPostDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogPostService.create(dto));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<BlogPostDto> updatePost(@PathVariable String id, @Valid @RequestBody UpdateBlogPostDto dto) {
        return ResponseEntity.ok(blogPostService.update(id, dto));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        blogPostService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments/post/{blogPostId}")
    public ResponseEntity<Page<BlogCommentDto>> listCommentsByPost(
            @PathVariable String blogPostId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogCommentService.listByPost(blogPostId, page, size));
    }

    @GetMapping("/comments/post/{blogPostId}/all")
    public ResponseEntity<List<BlogCommentDto>> getAllCommentsByPost(@PathVariable String blogPostId) {
        return ResponseEntity.ok(blogCommentService.findAllByPost(blogPostId));
    }

    @GetMapping("/comments/thread/{rootCommentId}")
    public ResponseEntity<List<BlogCommentDto>> getCommentThread(@PathVariable String rootCommentId) {
        return ResponseEntity.ok(blogCommentService.findThread(rootCommentId));
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<BlogCommentDto> getCommentById(@PathVariable String id) {
        return ResponseEntity.ok(blogCommentService.findById(id));
    }

    @PostMapping("/comments")
    public ResponseEntity<BlogCommentDto> createComment(@Valid @RequestBody CreateBlogCommentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogCommentService.create(dto));
    }

    @PostMapping("/comments/{id}/like")
    public ResponseEntity<BlogCommentDto> likeComment(@PathVariable String id) {
        return ResponseEntity.ok(blogCommentService.like(id));
    }

    @DeleteMapping("/comments/{id}/like")
    public ResponseEntity<BlogCommentDto> unlikeComment(@PathVariable String id) {
        return ResponseEntity.ok(blogCommentService.unlike(id));
    }

    @PatchMapping("/comments/{id}/moderate")
    public ResponseEntity<Void> moderateComment(
            @PathVariable String id,
            @RequestParam CommentStatus status,
            @RequestParam(required = false) String moderatorId,
            @RequestParam(required = false) String note) {
        blogCommentService.moderate(id, status, moderatorId, note);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        blogCommentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
