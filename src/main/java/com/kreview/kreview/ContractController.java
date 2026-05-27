package com.kreview.kreview;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

  @Autowired
  private ContractService contractService;
  @Autowired
  private GeminiService geminiService;

  @GetMapping
  public List<Contract> getAllContracts() {
    return contractService.getAllContracts();
  }

  @PostMapping
  public Contract submitContract(@RequestBody ContractRequest request) {
    return contractService.submitContract(request);
  }

  @GetMapping("/{id}")
  public Contract getContractById(@PathVariable Long id) {
    return contractService.getContractById(id);
  }

  @PostMapping("/{id}/analyze")
  public AnalysisResult analyzeContract(@PathVariable Long id) {
    Contract contract = contractService.getContractById(id);
    if (contract == null) {
      return null;
    }
    return contractService.analyzeContract(contract);
  }

  @GetMapping("/test-ai")
  public String testAi() {
    return geminiService.analyzeContractWithAI(
        "This agreement is made between Party A and Party B. All information shared shall remain confidential for 2 years. Either party may terminate this agreement with 7 days written notice. Party B shall indemnify Party A against all claims. Payment of $5000 is due within 30 days."
    );
  }

}