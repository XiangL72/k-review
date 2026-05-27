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
    String aiResponse = geminiService.analyzeContractWithAI(contract.getContent());
    AnalysisResult result = geminiService.parseAnalysisResponse(aiResponse, contract);
    return analysisResultRepository.save(result);
  }


}