package com.digitalhelper.repository;

import com.digitalhelper.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Feedback> findAllByOrderByCreatedAtDesc();

    long countByHelpfulTrue();
    long countByHelpfulFalse();

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();
}
