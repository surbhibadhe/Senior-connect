
package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.Resource;
import com.project.seniorconnect.Entity.ResourceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findAllByOrderByCreatedAtDesc();

    List<Resource> findByCategoryOrderByCreatedAtDesc(ResourceCategory category);

    List<Resource> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    Page<Resource> findByCategory(ResourceCategory category, Pageable pageable);

    @Query(
            value = "SELECT r FROM Resource r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR (r.description IS NOT NULL AND LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))",
            countQuery = "SELECT count(r) FROM Resource r WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR (r.description IS NOT NULL AND LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<Resource> findBySearch(@Param("search") String search, Pageable pageable);

    @Query(
            value = "SELECT r FROM Resource r WHERE r.category = :category AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR (r.description IS NOT NULL AND LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))))",
            countQuery = "SELECT count(r) FROM Resource r WHERE r.category = :category AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR (r.description IS NOT NULL AND LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))))"
    )
    Page<Resource> findByCategoryAndSearch(
            @Param("category") ResourceCategory category,
            @Param("search") String search,
            Pageable pageable
    );

    Page<Resource> findByUploadedById(Long userId, Pageable pageable);

    long countByUploadedById(Long userId);
}
