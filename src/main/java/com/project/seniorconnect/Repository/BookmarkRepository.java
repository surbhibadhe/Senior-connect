package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    Optional<Bookmark> findByUserIdAndResourceId(Long userId, Long resourceId);

    void deleteByUserIdAndResourceId(Long userId, Long resourceId);

    Page<Bookmark> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
}
