package com.kreview.kreview;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

  private final ContractService contractService;

  public ContractController(ContractService contractService) {
    this.contractService = contractService;
  }

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
  public ResponseEntity<?> analyzeContract(@PathVariable Long id) {
    Contract contract = contractService.getContractById(id);
    if (contract == null) {
      return ResponseEntity.status(404).body("Contract not found");
    }
    AnalysisResult result = contractService.analyzeContract(contract);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/analyzed")
  public List<Contract> getAnalyzedContracts() {
    return contractService.getAnalyzedContracts();
  }
}