package com.finSecure.controller;

import com.finSecure.dto.request.RecordRequest;
import com.finSecure.dto.response.RecordResponse;
import com.finSecure.entity.Category;
import com.finSecure.entity.RecordType;
import com.finSecure.service.RecordService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/finsecure/records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RecordController {

    private final RecordService recordService;

    /**
     * POST /api/records
     * ADMIN only — VIEWERs and ANALYST cannot create records.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecordResponse> create(
            @Valid @RequestBody RecordRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(request, auth));
    }

    /**
     * GET /api/records
     * All roles can list. ADMIN sees all users' records; others see only their own.
     * Supports optional filters: category, recordType, from, to, page, size.
     *
     * Example: GET /api/records?category=FOOD&recordType=EXPENSE&from=2025-01-01T00:00:00&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasAnyRole( 'ANALYST', 'ADMIN')")
    public ResponseEntity<Page<RecordResponse>> getAll(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) RecordType recordType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return ResponseEntity.ok(recordService.getRecords(auth, category, recordType, from, to, page, size));
    }

    /**
     * GET /api/records/{id}
     * All roles. Users can only fetch their own records; ADMIN can fetch any.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<RecordResponse> getById(
            @PathVariable UUID id,
            Authentication auth) {
        return ResponseEntity.ok(recordService.getById(id, auth));
    }

    /**
     * PUT /api/records/{id}
     * ADMIN (any record).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecordResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecordRequest request,
            Authentication auth) {
        return ResponseEntity.ok(recordService.update(id, request, auth));
    }

    /**
     * DELETE /api/records/{id}
     * ADMIN (any record).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(
            @PathVariable UUID id,
            Authentication auth) {
        recordService.delete(id, auth);
        return ResponseEntity.ok("Record deleted successfully");
    }
}