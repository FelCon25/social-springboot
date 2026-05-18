package com.social.backend.auth.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.social.backend.auth.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

        @Modifying
        @Query("update Session s set s.isActive = false, s.updatedAt = :now " +
                        "where s.id = :id and s.isActive = true")
        int deactivateById(@Param("id") Long id, @Param("now") Instant now);

        @Modifying
        @Query("update Session s set s.isActive = false, s.updatedAt = :now " +
                        "where s.user.id = :userId and s.isActive = true")
        int deactivateAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

        @Query("select s from Session s where s.isActive = true and s.user.id = :userId")
        List<Session> findActiveSessions(@Param("userId") long userId);
}
