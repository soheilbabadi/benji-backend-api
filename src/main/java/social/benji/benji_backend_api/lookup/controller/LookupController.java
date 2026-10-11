package social.benji.benji_backend_api.lookup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import social.benji.benji_backend_api.lookup.dto.CreateLookupDto;
import social.benji.benji_backend_api.lookup.dto.LookupDto;
import social.benji.benji_backend_api.lookup.dto.UpdateLookupDto;
import social.benji.benji_backend_api.lookup.service.LookupService;

import java.util.List;

/**
 * REST controller exposing the lookup/reference-data service layer.
 *
 * <p>Read endpoints are intended for client apps (dropdowns, filters);
 * write endpoints (POST/PATCH/PUT/DELETE) are admin operations.</p>
 */
@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "مقادیر مرجع", description = "داده‌های lookup و فهرست‌های مرجع سیستم")
public class LookupController {

    private final LookupService lookupService;

    // ------------------------------------------------------------------ read

    @Operation(summary = "دریافت تمام مقادیر فعال")
    @ApiResponse(responseCode = "200", description = "لیست مقادیر فعال با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<LookupDto>> listActive() {
        return ResponseEntity.ok(lookupService.listActive());
    }

    @Operation(summary = "دریافت دسته‌بندی‌های موجود")
    @ApiResponse(responseCode = "200", description = "لیست دسته‌بندی‌ها با موفقیت دریافت شد")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> listCategories() {
        return ResponseEntity.ok(lookupService.listCategories());
    }

    @Operation(summary = "دریافت یک مقدار مرجع بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مقدار مرجع با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LookupDto> findById(@PathVariable Long id) {
        log.info("Fetching lookup with id: {}", id);
        return ResponseEntity.ok(lookupService.findById(id));
    }

    @Operation(summary = "دریافت مقادیر فعال یک دسته‌بندی")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "لیست مقادیر با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "400", description = "نام دسته‌بندی نامعتبر است")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<LookupDto>> listByCategory(@PathVariable String category) {
        return ResponseEntity.ok(lookupService.listByCategory(category));
    }

    @Operation(summary = "دریافت همه مقادیر یک دسته‌بندی شامل غیرفعال‌ها (مدیریت)")
    @ApiResponse(responseCode = "200", description = "لیست کامل مقادیر با موفقیت دریافت شد")
    @GetMapping("/category/{category}/all")
    public ResponseEntity<List<LookupDto>> listAllByCategory(@PathVariable String category,
                                                             @RequestParam(defaultValue = "true") boolean includeInactive) {
        return ResponseEntity.ok(includeInactive
                ? lookupService.listAllByCategory(category)
                : lookupService.listByCategory(category));
    }

    @Operation(summary = "دریافت یک مقدار مرجع بر اساس دسته‌بندی و کد")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مقدار مرجع با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد")
    })
    @GetMapping("/category/{category}/code/{code}")
    public ResponseEntity<LookupDto> findByCategoryAndCode(@PathVariable String category,
                                                           @PathVariable String code) {
        return ResponseEntity.ok(lookupService.findByCategoryAndCode(category, code));
    }

    // ----------------------------------------------------------------- write

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ایجاد مقدار مرجع جدید")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "مقدار مرجع با موفقیت ایجاد شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است"),
            @ApiResponse(responseCode = "409", description = "مقدار تکراری در همین دسته‌بندی وجود دارد")
    })
    @PostMapping
    public ResponseEntity<LookupDto> create(@Valid @RequestBody CreateLookupDto dto) {
        log.info("Creating lookup item category={} code={}", dto.getCategory(), dto.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(lookupService.create(dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "به‌روزرسانی جزئی مقدار مرجع (فیلدهای null تغییر نمی‌کنند)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مقدار مرجع با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<LookupDto> update(@PathVariable Long id,
                                            @Valid @RequestBody UpdateLookupDto dto) {
        log.info("Partially updating lookup with id: {}", id);
        return ResponseEntity.ok(lookupService.update(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "فعال یا غیرفعال کردن مقدار مرجع")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "وضعیت فعال/غیرفعال به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد")
    })
    @PatchMapping("/{id}/active")
    public ResponseEntity<LookupDto> setActive(@PathVariable Long id,
                                               @RequestParam boolean active) {
        log.info("Setting lookup id={} active={}", id, active);
        return ResponseEntity.ok(lookupService.setActive(id, active));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "جایگزینی کامل مقدار مرجع")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مقدار مرجع با موفقیت جایگزین شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد"),
            @ApiResponse(responseCode = "409", description = "مقدار تکراری در همین دسته‌بندی وجود دارد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LookupDto> replace(@PathVariable Long id,
                                             @Valid @RequestBody CreateLookupDto dto) {
        log.info("Full replacement of lookup with id: {}", id);
        return ResponseEntity.ok(lookupService.update(id, toUpdateDto(dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "حذف مقدار مرجع بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "مقدار مرجع با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "مقدار مرجع یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteById(@PathVariable Long id) {
        log.info("Deleting lookup with id: {}", id);
        lookupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** Maps a creation payload onto a partial-update payload (category is immutable by design). */
    private static UpdateLookupDto toUpdateDto(CreateLookupDto create) {
        return UpdateLookupDto.builder()
                .code(create.getCode())
                .title(create.getTitle())
                .description(create.getDescription())
                .active(create.getActive())
                .sortOrder(create.getSortOrder())
                .build();
    }
}
