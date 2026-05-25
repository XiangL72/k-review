package com.kreview.kreview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ContractService {

  private List<Contract> contracts = new ArrayList<>();
  private Long nextId = 1L;

  public List<Contract> getAllContracts() {
    return contracts;
  }

  public Contract submitContract(ContractRequest request) {
    Contract contract = new Contract();
    contract.setId(nextId);
    contract.setContent(request.getContent());
    contract.setCreatedAt(LocalDateTime.now());

    contracts.add(contract);
    nextId++;

    return contract;
  }

  public Contract getContractById(Long id) {
    for (Contract contract : contracts) {
      if (contract.getId().equals(id)) {
        return contract;
      }
    }
    return null;
  }

  public AnalysisResult analyzeContract(String text) {
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
    result.setClauses(clauses);
    result.setOverallRiskScore(Math.min(riskScore, 10));
    result.setSummary("Analysis found " + clauses.size() + " clauses. "
        + (riskScore >= 7 ? "High risk contract."
        : riskScore >= 4 ? "Medium risk contract."
        : "Low risk contract."));

    return result;
  }

}