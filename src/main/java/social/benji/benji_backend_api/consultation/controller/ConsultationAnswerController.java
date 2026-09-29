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
import social.benji.benji_backend_api.consultation.dto.ConsultationAnswerDto;
import social.benji.benji_backend_api.consultation.service.ConsultationAnswerService;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-answers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "پاسخ‌های مشاوره", description = "مدیریت پاسخ‌های نهایی متخصصان")
public class ConsultationAnswerController {

    private final ConsultationAnswerService consultationAnswerService;

    @Operation(summary = "ایجاد پاسخ مشاوره جدید")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "پاسخ با موفقیت ایجاد شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationAnswerDto> create(@Valid @RequestBody ConsultationAnswerDto answerDto) {
        log.info("Creating consultation answer for expertId: {}", answerDto.getExpertId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationAnswerService.create(answerDto));
    }

    @Operation(summary = "دریافت پاسخ مشاوره بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پاسخ با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پاسخ یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationAnswerDto> findById(@PathVariable String id) {
        log.info("Fetching consultation answer with id: {}", id);
        return ResponseEntity.ok(consultationAnswerService.findById(id));
    }

    @Operation(summary = "دریافت تمام پاسخ‌های مشاوره")
    @ApiResponse(responseCode = "200", description = "لیست پاسخ‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationAnswerDto>> findAll() {
        return ResponseEntity.ok(consultationAnswerService.findAll());
    }

    @Operation(summary = "دریافت پاسخ مشاوره بر اساس شناسه متخصص")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پاسخ با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پاسخی برای این متخصص یافت نشد")
    })
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<ConsultationAnswerDto> findByExpertId(@PathVariable String expertId) {
        return ResponseEntity.ok(consultationAnswerService.findByExpertId(expertId));
    }

    @Operation(summary = "به‌روزرسانی پاسخ مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پاسخ با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "پاسخ یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationAnswerDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationAnswerDto answerDto) {
        log.info("Updating consultation answer with id: {}", id);
        answerDto.setId(id);
        return ResponseEntity.ok(consultationAnswerService.update(answerDto));
    }

    @Operation(summary = "حذف پاسخ مشاوره بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "پاسخ با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "پاسخ یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting consultation answer with id: {}", id);
        consultationAnswerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

