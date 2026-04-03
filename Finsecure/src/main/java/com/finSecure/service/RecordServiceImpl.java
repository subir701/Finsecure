package com.finSecure.service;

import com.finSecure.dto.request.RecordRequest;
import com.finSecure.dto.response.RecordResponse;
import com.finSecure.entity.Category;
import com.finSecure.entity.Record;
import com.finSecure.entity.RecordType;
import com.finSecure.entity.User;
import com.finSecure.exception.ApiException;
import com.finSecure.repository.RecordRepository;
import com.finSecure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordServiceImpl implements RecordService{

    private final UserRepository userRepository;
    private final RecordRepository recordRepository;

    @Transactional
    @Override
    public RecordResponse create(RecordRequest request, Authentication auth) {
        User user = getUser(auth);

        Record record = Record.builder()
                .amount(request.amount())
                .category(request.category())
                .recordType(request.recordType())
                .note(request.note())
                .user(user)
                .build();

        return toResponse(record);
    }

    @Override
    public Page<RecordResponse> getRecords(Authentication authentication, Category category, RecordType recordType, LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        boolean isAdmin = isAdmin(authentication);

        if(isAdmin){
            return recordRepository
                    .findAllByFilters(category, recordType,from, to, pageable)
                    .map(this::toResponse);
        }

        UUID userId = getUser(authentication).getUserId();
        return recordRepository
                .findByFilters(userId, category, recordType, from, to, pageable)
                .map(this::toResponse);
    }

    @Override
    public RecordResponse getById(UUID recordId, Authentication authentication) {
        Record record = findRecord(recordId);
        assertAccess(record, authentication);
        return toResponse(record);
    }

    @Transactional
    @Override
    public RecordResponse update(UUID recordId, RecordRequest request, Authentication authentication) {
        Record record = findRecord(recordId);
        assertOwnerOrAdmin(record, authentication);

        record.setAmount(request.amount());
        record.setCategory(request.category());
        record.setRecordType(request.recordType());
        record.setNote(request.note());

        return toResponse(recordRepository.save(record));
    }

    @Transactional
    @Override
    public void delelte(UUID recordId, Authentication authentication) {
        Record record = findRecord(recordId);
        assertOwnerOrAdmin(record, authentication);
        recordRepository.delete(record);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Record findRecord(UUID recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> ApiException.notFound("Record not found with id: " + recordId));
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Authenticated user not found"));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    /** Viewer and Analyst can read all records scoped to them; Admin can read all. */
    private void assertAccess(Record record, Authentication auth) {
        if (!isAdmin(auth) && !record.getUser().getEmail().equals(auth.getName())) {
            throw ApiException.forbidden("You don't have access to this record");
        }
    }

    /** Only the owner or an Admin may mutate a record. */
    private void assertOwnerOrAdmin(Record record, Authentication auth) {
        if (!isAdmin(auth) && !record.getUser().getEmail().equals(auth.getName())) {
            throw ApiException.forbidden("Only the record owner or an Admin can modify this record");
        }
    }

    private RecordResponse toResponse(Record record) {
        return new RecordResponse(
                record.getRecordId(),
                record.getAmount(),
                record.getCategory(),
                record.getRecordType(),
                record.getNote(),
                record.getCreatedAt()
        );
    }
}
