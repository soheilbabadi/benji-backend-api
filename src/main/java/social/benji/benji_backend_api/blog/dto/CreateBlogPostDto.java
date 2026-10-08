package social.benji.benji_backend_api.blog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social.benji.benji_backend_api.blog.domain.BlogPostModel;

/**
 * Creation DTO for blog posts (counterpart of BlogPostDto). Fields mirror what
 * BlogPostMapper.toEntity consumes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostDto {

    private String title;
    private String content;
    private List<String> tags;
    private List<String> categories;
    private List<String> attachmentIds;
    private BlogPostModel.Status status;
}
