package social.benji.benji_backend_api.blog.dto;

import social.benji.benji_backend_api.blog.domain.BlogCommentModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between BlogCommentModel and DTOs.
 */
public final class BlogCommentMapper {

    private BlogCommentMapper() {
        // Private constructor to prevent instantiation
    }

    public static BlogCommentDto toDto(BlogCommentModel model) {
        if (model == null) {
            return null;
        }

        return BlogCommentDto.builder()
                .id(model.getId())
                .blogPostId(model.getBlogPostId())
                .authorId(model.getAuthorId())
                .authorName(model.getAuthorName())
                .content(model.getContent())
                .parentId(model.getParentId())
                .rootCommentId(model.getRootCommentId())
                .topLevel(model.getTopLevel())
                .depth(model.getDepth())
                .repliesCount(model.getRepliesCount() != null ? model.getRepliesCount() : 0)
                .likesCount(model.getLikesCount() != null ? model.getLikesCount() : 0)
                .status(model.getStatus())
                .edited(model.getEdited())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public static List<BlogCommentDto> toDtoList(List<BlogCommentModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(BlogCommentMapper::toDto)
                .collect(Collectors.toList());
    }
}
