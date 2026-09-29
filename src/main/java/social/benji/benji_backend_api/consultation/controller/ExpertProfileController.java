package social.benji.benji_backend_api.consultation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.benji.benji_backend_api.consultation.domain.valueobject.ExpertVerificationStatus;
import social.benji.benji_backend_api.consultation.dto.ExpertProfileDto;
import social.benji.benji_backend_api.consultation.service.ExpertProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/expert-profiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "پروفایل متخصصان", description = "مدیریت پروفایل متخصصان تأییدشده")
public class ExpertProfileController {

    private final ExpertProfileService expertProfileService;

    @Operation(summary = "ایجاد پروفایل متخصص جدید")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "پروفایل با موفقیت ایجاد شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ExpertProfileDto> create(@Valid @RequestBody ExpertProfileDto expertProfileDto) {
        log.info("Creating expert profile for userId: {}", expertProfileDto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(expertProfileService.create(expertProfileDto));
    }

    @Operation(summary = "دریافت پروفایل متخصص بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پروفایل با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پروفایل یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpertProfileDto> findById(@PathVariable String id) {
        log.info("Fetching expert profile with id: {}", id);
        return ResponseEntity.ok(expertProfileService.findById(id));
    }

    @Operation(summary = "دریافت تمام پروفایل‌های متخصصان")
    @ApiResponse(responseCode = "200", description = "لیست پروفایل‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ExpertProfileDto>> findAll() {
        return ResponseEntity.ok(expertProfileService.findAll());
    }

    @Operation(summary = "دریافت پروفایل متخصص بر اساس شناسه کاربری")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پروفایل با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پروفایل یافت نشد")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ExpertProfileDto> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(expertProfileService.findByUserId(userId));
    }

    @Operation(summary = "دریافت متخصصان بر اساس وضعیت تأیید")
    @ApiResponse(responseCode = "200", description = "لیست متخصصان با موفقیت دریافت شد")
    @GetMapping("/status/{verificationStatus}")
    public ResponseEntity<List<ExpertProfileDto>> findByVerificationStatus(@PathVariable ExpertVerificationStatus verificationStatus) {
        return ResponseEntity.ok(expertProfileService.findByVerificationStatus(verificationStatus));
    }

    @Operation(summary = "دریافت تمام متخصصان فعال")
    @ApiResponse(responseCode = "200", description = "لیست متخصصان فعال با موفقیت دریافت شد")
    @GetMapping("/active")
    public ResponseEntity<List<ExpertProfileDto>> findAllActive() {
        return ResponseEntity.ok(expertProfileService.findAllActive());
    }

    @Operation(summary = "دریافت متخصصان بر اساس تخصص")
    @ApiResponse(responseCode = "200", description = "لیست متخصصان با موفقیت دریافت شد")
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<ExpertProfileDto>> findBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(expertProfileService.findBySpecialty(specialty));
    }

    @Operation(summary = "به‌روزرسانی پروفایل متخصص")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پروفایل با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "پروفایل یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpertProfileDto> update(
            @PathVariable String id,
            @Valid @RequestBody ExpertProfileDto expertProfileDto) {
        log.info("Updating expert profile with id: {}", id);
        expertProfileDto.setId(id);
        return ResponseEntity.ok(expertProfileService.update(expertProfileDto));
    }

    @Operation(summary = "حذف پروفایل متخصص بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "پروفایل با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "پروفایل یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting expert profile with id: {}", id);
        expertProfileService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

