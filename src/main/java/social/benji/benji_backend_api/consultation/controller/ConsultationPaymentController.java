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
import social.benji.benji_backend_api.consultation.domain.valueobject.PaymentStatus;
import social.benji.benji_backend_api.consultation.dto.ConsultationPaymentDto;
import social.benji.benji_backend_api.consultation.service.ConsultationPaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "پرداخت‌های مشاوره", description = "مدیریت پرداخت‌های مربوط به مشاوره")
public class ConsultationPaymentController {

    private final ConsultationPaymentService consultationPaymentService;

    @Operation(summary = "ثبت پرداخت جدید برای مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "پرداخت با موفقیت ثبت شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationPaymentDto> create(@Valid @RequestBody ConsultationPaymentDto paymentDto) {
        log.info("Creating payment for consultationId: {}", paymentDto.getConsultationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationPaymentService.create(paymentDto));
    }

    @Operation(summary = "دریافت پرداخت بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پرداخت با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پرداخت یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationPaymentDto> findById(@PathVariable String id) {
        log.info("Fetching payment with id: {}", id);
        return ResponseEntity.ok(consultationPaymentService.findById(id));
    }

    @Operation(summary = "دریافت تمام پرداخت‌ها")
    @ApiResponse(responseCode = "200", description = "لیست پرداخت‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationPaymentDto>> findAll() {
        return ResponseEntity.ok(consultationPaymentService.findAll());
    }

    @Operation(summary = "دریافت پرداخت بر اساس شناسه مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پرداخت با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پرداختی برای این مشاوره یافت نشد")
    })
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<ConsultationPaymentDto> findByConsultationId(@PathVariable String consultationId) {
        return ResponseEntity.ok(consultationPaymentService.findByConsultationId(consultationId));
    }

    @Operation(summary = "دریافت پرداخت بر اساس شناسه درگاه پرداخت")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پرداخت با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پرداخت یافت نشد")
    })
    @GetMapping("/provider/{providerPaymentId}")
    public ResponseEntity<ConsultationPaymentDto> findByProviderPaymentId(@PathVariable String providerPaymentId) {
        return ResponseEntity.ok(consultationPaymentService.findByProviderPaymentId(providerPaymentId));
    }

    @Operation(summary = "دریافت پرداخت بر اساس کلید idempotency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پرداخت با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "پرداخت یافت نشد")
    })
    @GetMapping("/idempotency/{idempotencyKey}")
    public ResponseEntity<ConsultationPaymentDto> findByIdempotencyKey(@PathVariable String idempotencyKey) {
        return ResponseEntity.ok(consultationPaymentService.findByIdempotencyKey(idempotencyKey));
    }

    @Operation(summary = "دریافت پرداخت‌ها بر اساس وضعیت")
    @ApiResponse(responseCode = "200", description = "لیست پرداخت‌ها با موفقیت دریافت شد")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ConsultationPaymentDto>> findByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(consultationPaymentService.findByStatus(status));
    }

    @Operation(summary = "به‌روزرسانی پرداخت")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "پرداخت با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "پرداخت یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationPaymentDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationPaymentDto paymentDto) {
        log.info("Updating payment with id: {}", id);
        paymentDto.setId(id);
        return ResponseEntity.ok(consultationPaymentService.update(paymentDto));
    }

    @Operation(summary = "حذف پرداخت بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "پرداخت با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "پرداخت یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting payment with id: {}", id);
        consultationPaymentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}



