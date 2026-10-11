package social.benji.benji_backend_api.blog.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import social.benji.benji_backend_api.blog.domain.BlogCommentModel;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;
import social.benji.benji_backend_api.blog.dto.BlogCommentDto;
import social.benji.benji_backend_api.blog.dto.BlogCommentMapper;
import social.benji.benji_backend_api.blog.dto.CreateBlogCommentDto;
import social.benji.benji_backend_api.blog.enums.CommentStatus;
import social.benji.benji_backend_api.blog.repository.BlogCommentRepository;
import social.benji.benji_backend_api.blog.repository.BlogPostRepository;
import social.benji.benji_backend_api.blog.service.BlogCommentService;
import social.benji.benji_backend_api.exception.BenjiCustomException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private static final int MAX_DEPTH = 10;

    private final BlogCommentRepository blogCommentRepository;
    private final BlogPostRepository blogPostRepository;

    @Override
    public Page<BlogCommentDto> listByPost(String blogPostId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return blogCommentRepository.findByBlogPostIdAndStatusAndDeletedFalse(blogPostId, CommentStatus.APPROVED, pageable)
                .map(BlogCommentMapper::toDto);
    }

    @Override
    public List<BlogCommentDto> findAllByPost(String blogPostId) {
        return BlogCommentMapper.toDtoList(blogCommentRepository.findByBlogPostIdAndDeletedFalse(blogPostId));
    }

    @Override
    public List<BlogCommentDto> findThread(String rootCommentId) {
        return BlogCommentMapper.toDtoList(blogCommentRepository.findByRootCommentIdAndDeletedFalse(rootCommentId));
    }

    @Override
    public BlogCommentDto findById(String id) {
        return BlogCommentMapper.toDto(getExisting(id));
    }

    @Override
    public BlogCommentDto create(CreateBlogCommentDto dto) {
        if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BenjiCustomException.ValidationException("Comment content must not be blank");
        }
        if (dto.getBlogPostId() == null || dto.getBlogPostId().isBlank()) {
            throw new BenjiCustomException.ValidationException("blogPostId is required");
        }

        // Idempotency: return the already-created comment for a repeated client request id.
        if (dto.getClientRequestId() != null && !dto.getClientRequestId().isBlank()) {
            var existing = blogCommentRepository.findByClientRequestId(dto.getClientRequestId());
            if (existing.isPresent()) {
                return BlogCommentMapper.toDto(existing.get());
            }
        }

        BlogPostModel post = blogPostRepository.findById(dto.getBlogPostId())
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .orElseThrow(() -> new BenjiCustomException.ParentNotFoundException("Blog post not found with id: " + dto.getBlogPostId()));

        BlogCommentModel parent = null;
        if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
            parent = getExisting(dto.getParentId());
            if (!post.getId().equals(parent.getBlogPostId())) {
                throw new BenjiCustomException.ValidationException("Parent comment belongs to a different post");
            }
            if (parent.getDepth() != null && parent.getDepth() >= MAX_DEPTH) {
                throw new BenjiCustomException.ValidationException("Comment thread nesting limit reached");
            }
        }

        BlogCommentModel model = BlogCommentModel.builder()
                .blogPostId(post.getId())
                .authorId(dto.getAuthorId())
                .authorName(dto.getAuthorName())
                .authorEmail(dto.getAuthorEmail())
                .content(dto.getContent())
                .parentId(parent != null ? parent.getId() : null)
                .rootCommentId(parent == null ? null : (Boolean.TRUE.equals(parent.getTopLevel()) ? parent.getId() : parent.getRootCommentId()))
                .topLevel(parent == null)
                .depth(parent == null ? 0 : parent.getDepth() + 1)
                .repliesCount(0)
                .likesCount(0)
                .status(CommentStatus.PENDING)
                .deleted(false)
                .edited(false)
                .clientRequestId(dto.getClientRequestId())
                .createdAt(Instant.now())
                .build();

        BlogCommentModel saved = blogCommentRepository.save(model);

        if (saved.getRootCommentId() == null) {
            // Top-level comment: it is its own thread root; store id so thread queries work.
            saved.setRootCommentId(saved.getId());
            saved = blogCommentRepository.save(saved);
        }
        if (parent != null) {
            parent.setRepliesCount((parent.getRepliesCount() == null ? 0 : parent.getRepliesCount()) + 1);
            blogCommentRepository.save(parent);
        }

        post.setCommentsCount((int) blogCommentRepository.countByBlogPostIdAndStatusAndDeletedFalse(post.getId(), CommentStatus.APPROVED));
        blogPostRepository.save(post);

        return BlogCommentMapper.toDto(saved);
    }

    @Override
    public void moderate(String id, CommentStatus status, String moderatorId, String note) {
        BlogCommentModel comment = getExisting(id);
        comment.setStatus(status);
        comment.setModeratedBy(moderatorId);
        comment.setModeratedAt(Instant.now());
        comment.setModerationNote(note);
        blogCommentRepository.save(comment);

        refreshPostCommentCount(comment.getBlogPostId());
    }

    @Override
    public void deleteById(String id) {
        BlogCommentModel comment = getExisting(id);
        comment.setDeleted(true);
        comment.setStatus(CommentStatus.DELETED);
        blogCommentRepository.save(comment);

        if (comment.getParentId() != null) {
            blogCommentRepository.findById(comment.getParentId()).ifPresent(parent -> {
                int count = parent.getRepliesCount() == null ? 0 : parent.getRepliesCount();
                parent.setRepliesCount(Math.max(0, count - 1));
                blogCommentRepository.save(parent);
            });
        }
        refreshPostCommentCount(comment.getBlogPostId());
    }

    private void refreshPostCommentCount(String blogPostId) {
        blogPostRepository.findById(blogPostId).ifPresent(post -> {
            post.setCommentsCount((int) blogCommentRepository.countByBlogPostIdAndStatusAndDeletedFalse(blogPostId, CommentStatus.APPROVED));
            blogPostRepository.save(post);
        });
    }

    private BlogCommentModel getExisting(String id) {
        return blogCommentRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .orElseThrow(() -> new BenjiCustomException.ResourceNotFoundException("Comment not found with id: " + id));
    }
}
