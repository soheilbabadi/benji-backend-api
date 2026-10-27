# Add blog service layer, comment model, and lookup service layer

**Branch:** `feature/blog-comment-lookup-services` → `dev`
**Commits:** 1 (`a44352c`) · **Files:** 19 changed (+989 / −7)

## Summary

Implements the work requested across this session:

1. **Optimized `BlogPostModel`** for performance and publishing workflow
2. **New `BlogCommentModel`** supporting threaded, moderated comments
3. **Service layer for blog posts & comments** (list / save / update / delete)
4. **Service layer for the `lookup` package** over the existing JPA entity

No controllers were added — services are ready to be wired into REST endpoints in a follow-up.

## Changes

### Blog — domain (`blog/domain`, `blog/enums`)
- **`BlogPostModel` (modified)**
  - New fields: `excerpt`, `coverImageUrl`, `readingTimeMinutes`, `likesCount`, publishing workflow (`published`, `publishedAt`, `scheduledFor`), soft delete (`deleted`)
  - Indexes: unique on `slug`; single-field on `authorId`, `tags`, `categories`, `blogPostStatus`; compound `{status, createdAt}` and `{authorId, createdAt}`
  - `@LastModifiedDate` on `updatedAt`
- **`BlogCommentModel` (new)** — `@Document(collection = "blog_comments")`
  - Threading: `parentId`, `rootCommentId` (whole thread in one query), `topLevel`, `depth`, `repliesCount`
  - Author snapshots (`authorName`/`authorEmail`/`authorAvatarUrl`) to avoid joins on read
  - Moderation: `status` (`CommentStatus`), `moderationNote`, `moderatedBy`, `moderatedAt`
  - Ops: `clientRequestId` (idempotent submissions), `ipAddress`/`userAgent`, soft delete preserving thread structure, `edited` flag
  - Indexes: `blogPostId`, `authorId`, `status`, compound `{blogPostId, createdAt}` and `{blogPostId, status, createdAt}`
- **`CommentStatus` (new enum):** `PENDING`, `APPROVED`, `SPAM`, `DELETED`

### Blog — data & service layers (new)
- **Repositories:** `BlogPostRepository`, `BlogCommentRepository` with derived query methods (paging by status/slug/author, thread lookups by `rootCommentId`, idempotency check by `clientRequestId`)
- **`BlogPostService` / `BlogPostServiceImpl`:** list (paged, published-only + admin variants), get by id/slug, save (auto slug generation w/ uniqueness, excerpt + reading-time calculation from content), **update with true PATCH semantics** (null fields untouched; excerpt/reading time recalculated when content changes; stable slugs), publish/unpublish, soft delete
- **`BlogCommentService` / `BlogCommentServiceImpl`:** save (thread wiring via `parentId`/`rootCommentId`/`depth`, idempotent re-submit returns existing comment), list per post (approved-only public view + full admin view), replies for a thread, moderation transitions, like counter, soft delete, and **sync of denormalized `BlogPostModel.commentsCount`**
- **DTOs/mappers:** `BlogCommentDto`, `CreateBlogCommentDto`, `BlogCommentMapper`; `BlogPostMapper` updated for new fields

### Lookup — service layer (new)
- **`LookupService` / `LookupServiceImpl`** over the existing JPA `tbl_lookups` entity (`@Transactional`, readOnly on queries):
  - `listActive()`, `listByCategory`, `listAllByCategory` (admin), `listCategories()` (distinct), `findById`, `findByCategoryAndCode`
  - `create`, `update` (PATCH-style partial update; `category` immutable to protect references), `setActive`, `deleteById`
  - Normalizes category/code to lowercase-trimmed; duplicates → `DuplicateEntityException`, missing rows → `ResourceNotFoundException`, blank inputs → `ValidationException` (all reusing existing `BenjiCustomException` types)
- **DTOs:** `LookupDto`, `CreateLookupDto` (bean validation matching column sizes), `UpdateLookupDto`, `LookupMapper`

## Verification

- Full blog + lookup packages compiled cleanly with `javac` against the project's local dependency jars (lombok, spring-data-mongodb, spring-data-jpa, jakarta.validation) — no errors or warnings.
- Backward compatible: `BlogPostMapper` and existing model consumers updated in-place; no breaking signature changes.
- ⚠️ Pre-existing repo issue (not introduced here): `pom.xml` targets Java 25 while the CI/dev environment has JDK 17, so a full `mvn package` fails even on the base commit. Recommend aligning `<java.version>` before merge or confirming the build agent runs JDK 25.

## Suggested follow-ups

- REST controllers for blog posts/comments and lookups
- Scheduled job to auto-publish posts where `scheduledFor <= now && !published`
- Tests for `BlogPostServiceImpl` slug/excerpt logic and `LookupServiceImpl` duplicate handling
