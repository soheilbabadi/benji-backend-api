package social.benji.benji_backend_api.blog.dto;

import social.benji.benji_backend_api.blog.domain.BlogPostModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between BlogPostModel and DTOs.
 */
public final class BlogPostMapper {

    private BlogPostMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Maps a BlogPostModel to a BlogPostDto.
     *
     * @param model the blog post entity
     * @return the corresponding DTO
     */
    public static BlogPostDto toDto(BlogPostModel model) {
        if (model == null) {
            return null;
        }

        return BlogPostDto.builder()
                .id(model.getId())
                .title(model.getTitle())
                .slug(model.getSlug())
                .content(model.getContent())
                .authorId(model.getAuthorId())
                .authorName(model.getAuthorName())
                .tags(model.getTags() != null ? model.getTags() : Collections.emptyList())
                .categories(model.getCategories() != null ? model.getCategories() : Collections.emptyList())
                .attachmentIds(model.getAttachmentIds() != null ? model.getAttachmentIds() : Collections.emptyList())
                .blogPostStatus(model.getBlogPostStatus())
                .views(model.getViews() != null ? model.getViews() : 0L)
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    /**
     * Maps a CreateBlogPostDto to a BlogPostModel.
     *
     * @param dto the creation DTO
     * @return the new blog post entity
     */
    public static BlogPostModel toEntity(CreateBlogPostDto dto) {
        if (dto == null) {
            return null;
        }

        return BlogPostModel.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .tags(dto.getTags() != null ? dto.getTags() : Collections.emptyList())
                .categories(dto.getCategories() != null ? dto.getCategories() : Collections.emptyList())
                .attachmentIds(dto.getAttachmentIds() != null ? dto.getAttachmentIds() : Collections.emptyList())
                .blogPostStatus(dto.getBlogPostStatus())
                .views(0L)
                .commentsCount(0)
                .featured(false)
                .build();
    }

    /**
     * Updates an existing BlogPostModel with data from UpdateBlogPostDto.
     *
     * @param dto   the update DTO
     * @param model the existing entity to update
     * @return the updated blog post entity
     */
    public static BlogPostModel updateEntity(UpdateBlogPostDto dto, BlogPostModel model) {
        if (dto == null || model == null) {
            return model;
        }

        model.setTitle(dto.getTitle());
        model.setContent(dto.getContent());
        model.setTags(dto.getTags() != null ? dto.getTags() : Collections.emptyList());
        model.setCategories(dto.getCategories() != null ? dto.getCategories() : Collections.emptyList());
        model.setAttachmentIds(dto.getAttachmentIds() != null ? dto.getAttachmentIds() : Collections.emptyList());
        model.setBlogPostStatus(dto.getBlogPostStatus());

        return model;
    }

    /**
     * Maps a list of BlogPostModel to a list of BlogPostDto.
     *
     * @param models the list of entities
     * @return the list of DTOs
     */
    public static List<BlogPostDto> toDtoList(List<BlogPostModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(BlogPostMapper::toDto)
                .collect(Collectors.toList());
    }
}
