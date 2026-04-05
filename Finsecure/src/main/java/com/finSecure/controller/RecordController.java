package com.finSecure.controller;

import com.finSecure.dto.request.RecordRequest;
import com.finSecure.dto.response.RecordResponse;
import com.finSecure.entity.Category;
import com.finSecure.entity.RecordType;
import com.finSecure.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

@RestController
@RequestMapping("/finsecure/records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Records", description = "Financial record management (CRUD & Filtering)")
public class RecordController {

    private final RecordService recordService;

    /**
     * POST /finsecure/records
     * ADMIN only — restricted from VIEWER and ANALYST.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new financial record (ADMIN only)",
            description = "Saves a new income or expense record to the system. Restricted to users with the ADMIN role."
    )
    public ResponseEntity<RecordResponse> create(
            @Valid @RequestBody RecordRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(request, auth));
    }

    /**
     * GET /finsecure/records
     * ADMIN and ANALYST only.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "List all records with dynamic filtering (ADMIN, ANALYST)",
            description = "Retrieves a paginated list of financial records. Allows filtering by category, record type, and date range. " +
                    "Provides the system-wide oversight required for administrative and analytical review."
    )
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
     * GET /finsecure/records/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "Get record details by ID (ADMIN, ANALYST)",
            description = "Fetches a single financial record using its unique UUID. Accessible only to high-privilege roles."
    )
    public ResponseEntity<RecordResponse> getById(
            @PathVariable UUID id,
            Authentication auth) {
        return ResponseEntity.ok(recordService.getById(id, auth));
    }

    /**
     * PUT /finsecure/records/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update an existing record (ADMIN only)",
            description = "Modifies the details of a specific financial record. Changes to amounts or categories are restricted to the ADMIN role."
    )
    public ResponseEntity<RecordResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RecordRequest request,
            Authentication auth) {
        return ResponseEntity.ok(recordService.update(id, request, auth));
    }

    /**
     * DELETE /finsecure/records/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a record (ADMIN only)",
            description = "Permanently removes a record from the database. This action is destructive and limited to the ADMIN role."
    )
    public ResponseEntity<String> delete(
            @PathVariable UUID id,
            Authentication auth) {
        recordService.delete(id, auth);
        return ResponseEntity.ok("Record deleted successfully");
    }
}