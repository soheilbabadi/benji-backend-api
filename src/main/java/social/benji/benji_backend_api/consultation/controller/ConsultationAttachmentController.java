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
import social.benji.benji_backend_api.consultation.dto.ConsultationAttachmentDto;
import social.benji.benji_backend_api.consultation.service.ConsultationAttachmentService;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-attachments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "پیوست‌های مشاوره", description = "مدیریت فایل‌های پیوست شده به مشاوره")
public class ConsultationAttachmentController {

    private final ConsultationAttachmentService consultationAttachmentService;

    @Operation(summary = "ایجاد پیوست جدید")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "پیوست با موفقیت ایجاد شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationAttachmentDto> create(@Valid @RequestBody ConsultationAttachmentDto attachmentDto) {
        log.info("Creating attachment for consultationId: {}", attachmentDto.getConsultationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationAttachmentService.create(attachmentDto));
    }

    @Operation(summary = "دریافت پیوست بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پیوست با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پیوست یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationAttachmentDto> findById(@PathVariable String id) {
        log.info("Fetching attachment with id: {}", id);
        return ResponseEntity.ok(consultationAttachmentService.findById(id));
    }

    @Operation(summary = "دریافت تمام پیوست‌ها")
    @ApiResponse(responseCode = "200", description = "لیست پیوست‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationAttachmentDto>> findAll() {
        return ResponseEntity.ok(consultationAttachmentService.findAll());
    }

    @Operation(summary = "دریافت پیوست‌ها بر اساس شناسه مشاوره")
    @ApiResponse(responseCode = "200", description = "لیست پیوست‌ها با موفقیت دریافت شد")
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<ConsultationAttachmentDto>> findByConsultationId(@PathVariable String consultationId) {
        return ResponseEntity.ok(consultationAttachmentService.findByConsultationId(consultationId));
    }

    @Operation(summary = "دریافت پیوست‌ها بر اساس آپلودکننده")
    @ApiResponse(responseCode = "200", description = "لیست پیوست‌ها با موفقیت دریافت شد")
    @GetMapping("/uploader/{uploadedBy}")
    public ResponseEntity<List<ConsultationAttachmentDto>> findByUploadedBy(@PathVariable String uploadedBy) {
        return ResponseEntity.ok(consultationAttachmentService.findByUploadedBy(uploadedBy));
    }

    @Operation(summary = "به‌روزرسانی پیوست")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پیوست با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "پیوست یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationAttachmentDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationAttachmentDto attachmentDto) {
        log.info("Updating attachment with id: {}", id);
        attachmentDto.setId(id);
        return ResponseEntity.ok(consultationAttachmentService.update(attachmentDto));
    }

    @Operation(summary = "حذف پیوست بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "پیوست با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "پیوست یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting attachment with id: {}", id);
        consultationAttachmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
