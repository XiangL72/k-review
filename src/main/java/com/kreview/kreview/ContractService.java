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
  @Autowired
  private GeminiService geminiService;

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
    try {
      AnalysisResult existing = analysisResultRepository.findByContractId(contract.getId());
      if (existing != null) {
        analysisResultRepository.delete(existing);
      }

      String aiResponse = geminiService.analyzeContractWithAI(contract.getContent());
      AnalysisResult result = geminiService.parseAnalysisResponse(aiResponse, contract);
      return analysisResultRepository.save(result);
    } catch (Exception e) {
      System.out.println("=== ANALYSIS FAILED ===");
      System.out.println("Error: " + e.getMessage());
      System.out.println("=== END ERROR ===");

      AnalysisResult fallback = new AnalysisResult();
      fallback.setContract(contract);
      fallback.setClauses(new ArrayList<>());
      fallback.setSummary("AI analysis failed: " + e.getMessage() + ". Please try again.");
      fallback.setOverallRiskScore(0);
      return fallback;
    }
  }

  public List<Contract> getAnalyzedContracts() {
    List<Contract> allContracts = contractRepository.findAll();
    List<Contract> analyzed = new ArrayList<>();
    for (Contract contract : allContracts) {
      AnalysisResult result = analysisResultRepository.findByContractId(contract.getId());
      if (result != null) {
        analyzed.add(contract);
      }
    }
    return analyzed;
  }


}