package com.kreview.kreview;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

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
    String jobId = contractService.submitAnalysisJob(contract);
    return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "PENDING"));
  }

  @GetMapping("/{id}/analysis")
  public ResponseEntity<?> getAnalysisByContractId(@PathVariable Long id) {
    AnalysisResult result = contractService.getAnalysisByContractId(id);
    if (result == null) {
      return ResponseEntity.status(404).body(Map.of(
          "error", "No analysis found for this contract",
          "contractId", id
      ));
    }
    return ResponseEntity.ok(result);
  }

  @GetMapping("/analyzed")
  public List<Contract> getAnalyzedContracts() {
    return contractService.getAnalyzedContracts();
  }

  @GetMapping("/search")
  public ResponseEntity<?> search(@RequestParam String q) {
    if (q == null || q.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of(
          "error", "Query parameter 'q' is required"
      ));
    }
    List<Contract> results = contractService.searchContracts(q);
    return ResponseEntity.ok(results);
  }

}