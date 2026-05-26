package com.kreview.kreview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractService {


  @Autowired
  private ContractRepository contractRepository;

  @Autowired
  private AnalysisResultRepository analysisResultRepository;

  public List<Contract> getAllContracts() {
    return contractRepository.findAll();
  }

  public Contract submitContract(ContractRequest request) {
    Contract contract = new Contract();
    contract.setContent(request.getContent());
    contract.setCreatedAt(LocalDateTime.now());

    return contractRepository.save(contract);
  }

  public Contract getContractById(Long id) {
    return contractRepository.findById(id).orElse(null);
  }

  public AnalysisResult analyzeContract(Contract contract) {
    String text = contract.getContent();
    List<Clause> clauses = new ArrayList<>();

    String[] sentences = text.split("\\.");

    for (String sentence : sentences) {
      String lower = sentence.toLowerCase().trim();

      if (lower.contains("confidential") || lower.contains("non-disclosure")) {
        Clause clause = new Clause();
        clause.setType("Confidentiality");
        clause.setText(sentence.trim());
        clause.setRiskLevel("LOW");
        clauses.add(clause);
      }

      if (lower.contains("terminat")) {
        Clause clause = new Clause();
        clause.setType("Termination");
        clause.setText(sentence.trim());
        clause.setRiskLevel("HIGH");
        clauses.add(clause);
      }

      if (lower.contains("governing law") || lower.contains("jurisdiction")) {
        Clause clause = new Clause();
        clause.setType("Governing Law");
        clause.setText(sentence.trim());
        clause.setRiskLevel("LOW");
        clauses.add(clause);
      }

      if (lower.contains("liability") || lower.contains("indemnif")) {
        Clause clause = new Clause();
        clause.setType("Liability");
        clause.setText(sentence.trim());
        clause.setRiskLevel("MEDIUM");
        clauses.add(clause);
      }

      if (lower.contains("payment") || lower.contains("compensation")) {
        Clause clause = new Clause();
        clause.setType("Payment");
        clause.setText(sentence.trim());
        clause.setRiskLevel("MEDIUM");
        clauses.add(clause);
      }
    }

    int riskScore = 0;
    for (Clause clause : clauses) {
      if (clause.getRiskLevel().equals("HIGH")) {
        riskScore += 3;
      } else if (clause.getRiskLevel().equals("MEDIUM")) {
        riskScore += 2;
      } else {
        riskScore += 1;
      }
    }

    AnalysisResult result = new AnalysisResult();
    result.setContract(contract);  // link to the contract
    result.setClauses(clauses);
    result.setOverallRiskScore(Math.min(riskScore, 10));
    result.setSummary("Analysis found " + clauses.size() + " clauses. "
        + (riskScore >= 7 ? "High risk contract."
        : riskScore >= 4 ? "Medium risk contract."
        : "Low risk contract."));

    return analysisResultRepository.save(result);  // save to database
  }
}