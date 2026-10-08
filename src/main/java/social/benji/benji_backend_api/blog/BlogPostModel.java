package social.benji.benji_backend_api.blog;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Document(collection = "blog_posts")
public class BlogPostModel {

    @Id
    private String id;

    @Indexed
    private String title;

    @Indexed(unique = true)
    private String slug;

    @Field("content")
    private String content;

    private String excerpt;

    private String featuredImage;

    public enum Status {
        DRAFT,
        PENDING_REVIEW,
        PUBLISHED,
        ARCHIVED
    }

    @Indexed
    private Status status = Status.DRAFT;

    private Instant publishedAt;

    @Indexed
    private String authorId;

    private Set<String> categoryIds;

    private Set<String> tagIds;

    private Integer readingTimeMinutes;

    @Indexed
    private Boolean featured = false;

    @org.springframework.data.annotation.CreatedDate
    private Instant createdAt;

    @org.springframework.data.annotation.LastModifiedDate
    private Instant updatedAt;
}
