package com.finSecure.repository;

import com.finSecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByIsActive(Boolean isActive);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = :status WHERE u.userId = :userId")
    int updateUserStatus(UUID userId, Boolean status);

}
