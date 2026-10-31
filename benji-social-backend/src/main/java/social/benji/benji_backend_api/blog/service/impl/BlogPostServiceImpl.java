package social.benji.benji_backend_api.blog.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.benji.benji_backend_api.blog.domain.BlogCommentModel;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;
import social.benji.benji_backend_api.blog.dto.BlogPostDto;
import social.benji.benji_backend_api.blog.dto.BlogPostMapper;
import social.benji.benji_backend_api.blog.dto.CreateBlogPostDto;
import social.benji.benji_backend_api.blog.dto.UpdateBlogPostDto;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;
import social.benji.benji_backend_api.blog.repository.BlogCommentRepository;
import social.benji.benji_backend_api.blog.repository.BlogPostRepository;
import social.benji.benji_backend_api.blog.service.BlogPostService;
import social.benji.benji_backend_api.exception.BenjiCustomException;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter.UserPrincipal;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService {

    private static final int WORDS_PER_MINUTE = 200;
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;

    @Override
    public Page<BlogPostDto> list(BlogPostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return blogPostRepository.findByBlogPostStatus(status, pageable)
                .map(BlogPostMapper::toDto);
    }

    @Override
    public Page<BlogPostDto> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return list(BlogPostStatus.PUBLISHED ,page,size);
    }

    @Override
    public BlogPostDto findById(String id) {
        return blogPostRepository.findById(id)
                .map(BlogPostMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with id: " + id));
    }

    @Override
    public BlogPostDto findBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
                .map(BlogPostMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with slug: " + slug));
    }

    @Override
    public List<BlogPostDto> findByAuthorId(String authorId) {
        return BlogPostMapper.toDtoList(blogPostRepository.findByAuthorId(authorId));
    }

    @Override
    public BlogPostDto create(CreateBlogPostDto dto, UserPrincipal creator) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BenjiCustomException.ValidationException("Blog post title must not be blank");
        }

        BlogPostModel model = BlogPostMapper.toEntity(dto, creator);
        model.setSlug(buildUniqueSlug(dto.getTitle()));
        model.setExcerpt(buildExcerpt(dto.getContent()));
        model.setReadingTimeMinutes(estimateReadingTimeMinutes(dto.getContent()));
        model.setPublished(BlogPostStatus.PUBLISHED == model.getBlogPostStatus());
        if (Boolean.TRUE.equals(model.getPublished())) {
            model.setPublishedAt(Instant.now());
        }

        model.setCreatedAt(Instant.now());

        return BlogPostMapper.toDto(blogPostRepository.save(model));
    }

    @Override
    public BlogPostDto update(String id, UpdateBlogPostDto dto, UserPrincipal editor) {
        BlogPostModel model = blogPostRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with id: " + id));
        requireOwnership(model, editor, "update");

        boolean wasPublished = BlogPostStatus.PUBLISHED == model.getBlogPostStatus();
        BlogPostMapper.updateEntity(dto, model);
        if (!wasPublished && BlogPostStatus.PUBLISHED == model.getBlogPostStatus()) {
            model.setPublished(true);
            model.setPublishedAt(Instant.now());
        }
        if (dto.getContent() != null) {
            model.setExcerpt(buildExcerpt(dto.getContent()));
            model.setReadingTimeMinutes(estimateReadingTimeMinutes(dto.getContent()));
        }

        return BlogPostMapper.toDto(blogPostRepository.save(model));
    }

    @Override
    @Transactional
    public void deleteById(String id, UserPrincipal requester) {
        BlogPostModel post = blogPostRepository.findById(id)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with id: " + id));
        requireOwnership(post, requester, "delete");

        // Remove all comments associated with the post from the database, then the post itself.
        List<BlogCommentModel> comments = blogCommentRepository.findByBlogPostId(post.getId());
        if (!comments.isEmpty()) {
            blogCommentRepository.deleteAll(comments);
        }
        blogPostRepository.deleteById(post.getId());
    }

    /**
     * Only the author may modify their own post; ROLE_ADMIN bypasses the check.
     * Throws ForbiddenException (mapped to HTTP 403) otherwise.
     */
    private void requireOwnership(BlogPostModel post, UserPrincipal requester, String action) {
        if (isAdmin(requester)) {
            return;
        }
        String callerId = requester != null && requester.id() != null ? requester.id().toString() : null;
        if (callerId == null || !callerId.equals(post.getAuthorId())) {
            throw new BenjiCustomException.ResourceForbiddenException(
                    "Only the author or an admin can " + action + " this blog post");
        }
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal != null && principal.authorities() != null
                && principal.authorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    private String buildUniqueSlug(String title) {
        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "post";
        }
        String slug = base;
        int suffix = 1;
        while (blogPostRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String buildExcerpt(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String plain = content.replaceAll("[#*_>`\\[\\]]", " ").trim();
        return plain.length() <= 200 ? plain : plain.substring(0, 200).trim() + "...";
    }

    private Integer estimateReadingTimeMinutes(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        long words = content.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) words / WORDS_PER_MINUTE));
    }
}
