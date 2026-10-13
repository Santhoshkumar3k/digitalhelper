package com.digitalhelper.repository;

import com.digitalhelper.model.ScamAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScamAnalysisLogRepository extends JpaRepository<ScamAnalysisLog, Long> {
    List<ScamAnalysisLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ScamAnalysisLog> findTop20ByOrderByCreatedAtDesc();
    long countByRiskLevel(String riskLevel);
}
