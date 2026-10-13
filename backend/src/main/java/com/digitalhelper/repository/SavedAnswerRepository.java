package com.digitalhelper.repository;

import com.digitalhelper.model.SavedAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedAnswerRepository extends JpaRepository<SavedAnswer, Long> {
    List<SavedAnswer> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SavedAnswer> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);

    Optional<SavedAnswer> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT s FROM SavedAnswer s WHERE s.user.id = :userId AND " +
           "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<SavedAnswer> searchByUserIdAndQuery(@Param("userId") Long userId, @Param("query") String query);
}
