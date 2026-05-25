package com.kreview.kreview;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

  @Autowired
  private ContractService contractService;

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
    return contractService.analyzeContract(contract.getContent());
  }
}