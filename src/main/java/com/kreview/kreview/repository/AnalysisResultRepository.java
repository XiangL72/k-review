package com.kreview.kreview.repository;

import com.kreview.kreview.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
  AnalysisResult findByContractId(Long contractId);
}