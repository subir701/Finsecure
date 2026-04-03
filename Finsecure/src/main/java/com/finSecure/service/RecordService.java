package com.finSecure.service;

import com.finSecure.dto.request.RecordRequest;
import com.finSecure.dto.response.RecordResponse;
import com.finSecure.entity.Category;
import com.finSecure.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RecordService {

    RecordResponse create(RecordRequest request, Authentication auth);
    Page<RecordResponse> getRecords(Authentication authentication, Category category, RecordType recordType, LocalDateTime from, LocalDateTime to, int page, int size);
    RecordResponse getById(UUID recordId, Authentication authentication);
    RecordResponse update(UUID recordId, RecordRequest request, Authentication authentication);
    void delelte(UUID recordId, Authentication authentication);

}
