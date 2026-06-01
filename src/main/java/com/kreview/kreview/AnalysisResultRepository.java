package com.kreview.kreview;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
  AnalysisResult findByContractId(Long contractId);
}