package social.benji.benji_backend_api.blog.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;
import social.benji.benji_backend_api.blog.dto.BlogPostDto;
import social.benji.benji_backend_api.blog.dto.BlogPostMapper;
import social.benji.benji_backend_api.blog.dto.CreateBlogPostDto;
import social.benji.benji_backend_api.blog.dto.UpdateBlogPostDto;
import social.benji.benji_backend_api.blog.enums.BlogPostStatus;
import social.benji.benji_backend_api.blog.repository.BlogPostRepository;
import social.benji.benji_backend_api.blog.service.BlogPostService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService {

    private static final int WORDS_PER_MINUTE = 200;
    private final BlogPostRepository blogPostRepository;

    @Override
    public Page<BlogPostDto> list(BlogPostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return blogPostRepository.findByBlogPostStatusAndDeletedFalse(status, pageable)
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
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .map(BlogPostMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with id: " + id));
    }

    @Override
    public BlogPostDto findBySlug(String slug) {
        return blogPostRepository.findBySlug(slug)
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .map(BlogPostMapper::toDto)
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with slug: " + slug));
    }

    @Override
    public List<BlogPostDto> findByAuthorId(String authorId) {
        return BlogPostMapper.toDtoList(blogPostRepository.findByAuthorIdAndDeletedFalse(authorId));
    }

    @Override
    public BlogPostDto create(CreateBlogPostDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BenjiCustomException.ValidationException("Blog post title must not be blank");
        }

        BlogPostModel model = BlogPostMapper.toEntity(dto);
        model.setSlug(buildUniqueSlug(dto.getTitle()));
        model.setExcerpt(buildExcerpt(dto.getContent()));
        model.setReadingTimeMinutes(estimateReadingTimeMinutes(dto.getContent()));
        model.setPublished(BlogPostStatus.PUBLISHED == model.getBlogPostStatus());
        if (Boolean.TRUE.equals(model.getPublished())) {
            model.setPublishedAt(Instant.now());
        }
        model.setDeleted(false);
        model.setCreatedAt(Instant.now());

        return BlogPostMapper.toDto(blogPostRepository.save(model));
    }

    @Override
    public BlogPostDto update(String id, UpdateBlogPostDto dto) {
        BlogPostModel model = blogPostRepository.findById(id)
                .filter(post -> !Boolean.TRUE.equals(post.getDeleted()))
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Blog post not found with id: " + id));

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
    public void deleteById(String id) {
        blogPostRepository.deleteById(id);
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
