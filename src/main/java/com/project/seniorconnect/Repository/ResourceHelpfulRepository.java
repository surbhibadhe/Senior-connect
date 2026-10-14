package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.ResourceHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResourceHelpfulRepository extends JpaRepository<ResourceHelpful, Long> {

    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    Optional<ResourceHelpful> findByUserIdAndResourceId(Long userId, Long resourceId);

    void deleteByUserIdAndResourceId(Long userId, Long resourceId);

    long countByResourceId(Long resourceId);

    @Modifying
    @Query("DELETE FROM ResourceHelpful h WHERE h.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
}
