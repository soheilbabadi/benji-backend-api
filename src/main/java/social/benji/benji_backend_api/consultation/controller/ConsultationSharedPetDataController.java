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
import social.benji.benji_backend_api.consultation.domain.valueobject.PetDataType;
import social.benji.benji_backend_api.consultation.dto.ConsultationSharedPetDataDto;
import social.benji.benji_backend_api.consultation.service.ConsultationSharedPetDataService;

import java.util.List;

@RestController
@RequestMapping("/api/consultation-shared-pet-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "اطلاعات حیوان خانگی اشتراک‌گذاری‌شده", description = "مدیریت داده‌های حیوان خانگی که برای مشاوره به اشتراک گذاشته شده‌اند")
public class ConsultationSharedPetDataController {

    private final ConsultationSharedPetDataService consultationSharedPetDataService;

    @Operation(summary = "ثبت داده حیوان خانگی برای مشاوره")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "داده با موفقیت ثبت شد"),
            @ApiResponse(responseCode = "400", description = "اطلاعات ورودی نامعتبر است")
    })
    @PostMapping
    public ResponseEntity<ConsultationSharedPetDataDto> create(@Valid @RequestBody ConsultationSharedPetDataDto sharedPetDataDto) {
        log.info("Creating shared pet data for consultationId: {}", sharedPetDataDto.getConsultationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationSharedPetDataService.create(sharedPetDataDto));
    }

    @Operation(summary = "دریافت داده حیوان خانگی بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "داده با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "داده یافت نشد")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationSharedPetDataDto> findById(@PathVariable String id) {
        log.info("Fetching shared pet data with id: {}", id);
        return ResponseEntity.ok(consultationSharedPetDataService.findById(id));
    }

    @Operation(summary = "دریافت تمام داده‌های حیوان خانگی اشتراک‌گذاری‌شده")
    @ApiResponse(responseCode = "200", description = "لیست داده‌ها با موفقیت دریافت شد")
    @GetMapping
    public ResponseEntity<List<ConsultationSharedPetDataDto>> findAll() {
        return ResponseEntity.ok(consultationSharedPetDataService.findAll());
    }

    @Operation(summary = "دریافت داده‌های حیوان خانگی بر اساس شناسه مشاوره")
    @ApiResponse(responseCode = "200", description = "لیست داده‌ها با موفقیت دریافت شد")
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<ConsultationSharedPetDataDto>> findByConsultationId(@PathVariable String consultationId) {
        return ResponseEntity.ok(consultationSharedPetDataService.findByConsultationId(consultationId));
    }

    @Operation(summary = "دریافت داده حیوان خانگی بر اساس شناسه مشاوره و نوع داده")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "داده با موفقیت دریافت شد"),
            @ApiResponse(responseCode = "404", description = "داده یافت نشد")
    })
    @GetMapping("/consultation/{consultationId}/type/{dataType}")
    public ResponseEntity<ConsultationSharedPetDataDto> findByConsultationIdAndDataType(
            @PathVariable String consultationId,
            @PathVariable PetDataType dataType) {
        return ResponseEntity.ok(consultationSharedPetDataService.findByConsultationIdAndDataType(consultationId, dataType));
    }

    @Operation(summary = "به‌روزرسانی داده حیوان خانگی")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "داده با موفقیت به‌روزرسانی شد"),
            @ApiResponse(responseCode = "404", description = "داده یافت نشد")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationSharedPetDataDto> update(
            @PathVariable String id,
            @Valid @RequestBody ConsultationSharedPetDataDto sharedPetDataDto) {
        log.info("Updating shared pet data with id: {}", id);
        sharedPetDataDto.setId(id);
        return ResponseEntity.ok(consultationSharedPetDataService.update(sharedPetDataDto));
    }

    @Operation(summary = "حذف داده حیوان خانگی بر اساس شناسه")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "داده با موفقیت حذف شد"),
            @ApiResponse(responseCode = "404", description = "داده یافت نشد")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        log.info("Deleting shared pet data with id: {}", id);
        consultationSharedPetDataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }






}
