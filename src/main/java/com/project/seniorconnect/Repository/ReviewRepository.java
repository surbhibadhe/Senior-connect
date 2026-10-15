package com.project.seniorconnect.Repository;

import com.project.seniorconnect.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user ORDER BY r.createdAt DESC")
    List<Review> findAllWithUserOrderByCreatedAtDesc();
}
