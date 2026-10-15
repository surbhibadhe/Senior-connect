package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.CommentUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CommentUpvoteRepository extends JpaRepository<CommentUpvote, Long> {

    long countByCommentId(Long commentId);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    Optional<CommentUpvote> findByCommentIdAndUserId(Long commentId, Long userId);

    @Modifying
    @Query("DELETE FROM CommentUpvote u WHERE u.comment.id = :commentId AND u.user.id = :userId")
    void deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CommentUpvote u WHERE u.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);

    @Modifying
    @Query("DELETE FROM CommentUpvote u WHERE u.comment.resource.id = :resourceId")
    void deleteByResourceId(@Param("resourceId") Long resourceId);

    @Query("SELECT u.comment.id FROM CommentUpvote u WHERE u.user.id = :userId AND u.comment.resource.id = :resourceId")
    Set<Long> findUpvotedCommentIdsByUserAndResource(@Param("userId") Long userId, @Param("resourceId") Long resourceId);

    @Query("SELECT u.comment.id, COUNT(u) FROM CommentUpvote u WHERE u.comment.resource.id = :resourceId GROUP BY u.comment.id")
    List<Object[]> countUpvotesByResourceId(@Param("resourceId") Long resourceId);
}
