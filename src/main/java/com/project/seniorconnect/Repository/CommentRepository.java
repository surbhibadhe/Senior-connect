package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user LEFT JOIN FETCH c.parentComment WHERE c.resource.id = :resourceId ORDER BY c.createdAt ASC")
    List<Comment> findByResourceIdWithUserAndParent(@Param("resourceId") Long resourceId);

    long countByResourceId(Long resourceId);

    long countByResourceIdAndDeletedFalse(Long resourceId);

    boolean existsByParentCommentId(Long parentId);

    List<Comment> findByParentCommentId(Long parentId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);
}
