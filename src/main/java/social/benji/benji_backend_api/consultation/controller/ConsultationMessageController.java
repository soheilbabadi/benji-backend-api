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
import social.benji.benji_backend_api.consultation.dto.ConsultationMessageDto;
import social.benji.benji_backend_api.consultation.service.ConsultationMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "پیام‌های مشاوره", description = "مدیریت پیام‌های مکالمه در مشاوره")
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;

    @Operation(summary = "ارسال پیام جدید در مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "پیام با موفقیت ارسال شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationMessageDto> create(@Valid @RequestBody ConsultationMessageDto messageDto) {
        log.info("Creating message for consultationId: {}", messageDto.getConsultationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationMessageService.create(messageDto));
    }

    @Operation(summary = "دریافت پیام بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پیام با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پیام یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationMessageDto> findById(@PathVariable String id) {
        log.info("Fetching message with id: {}", id);
        return ResponseEntity.ok(consultationMessageService.findById(id));
    }

    @Operation(summary = "دریافت تمام پیام‌ها")
    @ApiResponse(responseCode = "200", description = "لیست پیام‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationMessageDto>> findAll() {
        return ResponseEntity.ok(consultationMessageService.findAll());
    }

    @Operation(summary = "دریافت پیام‌های یک مشاوره (مرتب‌شده بر اساس زمان)")
    @ApiResponse(responseCode = "200", description = "لیست پیام‌ها با موفقیت دریافت شد")
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<ConsultationMessageDto>> findByConsultationId(@PathVariable String consultationId) {
        return ResponseEntity.ok(consultationMessageService.findByConsultationId(consultationId));
    }

    @Operation(summary = "دریافت پیام‌های یک مشاوره از یک فرستنده خاص")
    @ApiResponse(responseCode = "200", description = "لیست پیام‌ها با موفقیت دریافت شد")
    @GetMapping("/consultation/{consultationId}/sender/{senderId}")
    public ResponseEntity<List<ConsultationMessageDto>> findByConsultationIdAndSenderId(
            @PathVariable String consultationId,
            @PathVariable String senderId) {
        return ResponseEntity.ok(consultationMessageService.findByConsultationIdAndSenderId(consultationId, senderId));
    }

    @Operation(summary = "به‌روزرسانی پیام")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پیام با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "پیام یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationMessageDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationMessageDto messageDto) {
        log.info("Updating message with id: {}", id);
        messageDto.setId(id);
        return ResponseEntity.ok(consultationMessageService.update(messageDto));
    }

    @Operation(summary = "حذف پیام بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "پیام با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "پیام یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting message with id: {}", id);
        consultationMessageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

