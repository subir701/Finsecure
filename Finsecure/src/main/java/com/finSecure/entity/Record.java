package com.finSecure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "records")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID recordId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType recordType;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String note;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
        if(this.transactionDate == null){
            this.transactionDate = LocalDate.now();
        }
    }
}
