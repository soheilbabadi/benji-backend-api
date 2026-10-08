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
import social.benji.benji_backend_api.consultation.domain.valueobject.ConsultationStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationDto;
import social.benji.benji_backend_api.consultation.service.ConsultationService;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "مشاوره‌ها", description = "مدیریت مشاوره‌های پزشکی")
public class ConsultationController {

    private final ConsultationService consultationService;

    @Operation(summary = "ایجاد مشاوره جدید")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "مشاوره با موفقیت ایجاد شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationDto> create(@Valid @RequestBody ConsultationDto consultationDto) {
        log.info("Creating consultation for ownerId: {}", consultationDto.getOwnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.create(consultationDto));
    }

    @Operation(summary = "دریافت مشاوره بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مشاوره با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "مشاوره یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDto> findById(@PathVariable String id) {
        log.info("Fetching consultation with id: {}", id);
        return ResponseEntity.ok(consultationService.findById(id));
    }

    @Operation(summary = "دریافت تمام مشاوره‌ها")
    @ApiResponse(responseCode = "200", description = "لیست مشاوره‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationDto>> findAll() {
        return ResponseEntity.ok(consultationService.findAll());
    }

    @Operation(summary = "دریافت مشاوره‌ها بر اساس شناسه صاحب حیوان")
    @ApiResponse(responseCode = "200", description = "لیست مشاوره‌ها با موفقیت دریافت شد")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ConsultationDto>> findByOwnerId(@PathVariable String ownerId) {
        return ResponseEntity.ok(consultationService.findByOwnerId(ownerId));
    }

    @Operation(summary = "دریافت مشاوره‌ها بر اساس شناسه متخصص")
    @ApiResponse(responseCode = "200", description = "لیست مشاوره‌ها با موفقیت دریافت شد")
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<List<ConsultationDto>> findByAssignedExpertId(@PathVariable String expertId) {
        return ResponseEntity.ok(consultationService.findByAssignedExpertId(expertId));
    }

    @Operation(summary = "دریافت مشاوره‌ها بر اساس وضعیت")
    @ApiResponse(responseCode = "200", description = "لیست مشاوره‌ها با موفقیت دریافت شد")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ConsultationDto>> findByStatus(@PathVariable ConsultationStatus status) {
        return ResponseEntity.ok(consultationService.findByStatus(status));
    }

    @Operation(summary = "دریافت مشاوره‌ها بر اساس شناسه صاحب حیوان و وضعیت")
    @ApiResponse(responseCode = "200", description = "لیست مشاوره‌ها با موفقیت دریافت شد")
    @GetMapping("/owner/{ownerId}/status/{status}")
    public ResponseEntity<List<ConsultationDto>> findByOwnerIdAndStatus(
            @PathVariable String ownerId,
            @PathVariable ConsultationStatus status) {
        return ResponseEntity.ok(consultationService.findByOwnerIdAndStatus(ownerId, status));
    }

    @Operation(summary = "به‌روزرسانی مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "مشاوره با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "مشاوره یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationDto consultationDto) {
        log.info("Updating consultation with id: {}", id);
        consultationDto.setId(id);
        return ResponseEntity.ok(consultationService.update(consultationDto));
    }

    @Operation(summary = "حذف مشاوره بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "مشاوره با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "مشاوره یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting consultation with id: {}", id);
        consultationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

