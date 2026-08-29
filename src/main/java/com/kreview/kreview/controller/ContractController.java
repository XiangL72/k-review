package com.kreview.kreview.controller;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.Contract;
import com.kreview.kreview.ContractRequest;
import com.kreview.kreview.ContractType;
import com.kreview.kreview.PartyRole;
import com.kreview.kreview.service.ContractService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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
  public Contract submitContract(@Valid @RequestBody ContractRequest request) {
    return contractService.submitContract(request);
  }

  @PostMapping("/upload")
  public Contract uploadContract(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) ContractType contractType,
                                  @RequestParam(required = false) PartyRole partyRole,
                                  @RequestParam(required = false) String partyRoleCustomLabel) {
    return contractService.submitContractFromFile(file, contractType, partyRole, partyRoleCustomLabel);
  }

  @GetMapping("/types")
  public Map<String, Object> getContractTypes() {
    List<Map<String, Object>> types = Arrays.stream(ContractType.values())
        .map(type -> Map.<String, Object>of("type", type, "roles", type.validRoles()))
        .toList();

    return Map.of(
        "types", types,
        "genericRoles", PartyRole.GENERIC_ROLES
    );
  }

  @GetMapping("/{id}")
  public Contract getContractById(@PathVariable Long id) {
    return contractService.getContractById(id);
  }

  @PostMapping("/{id}/analyze")
  public ResponseEntity<?> analyzeContract(@PathVariable Long id) {
    Contract contract = contractService.getContractById(id);
    String jobId = contractService.submitAnalysisJob(contract);
    return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "PENDING"));
  }

  @GetMapping("/{id}/analysis")
  public ResponseEntity<?> getAnalysisByContractId(@PathVariable Long id) {
    AnalysisResult result = contractService.getAnalysisByContractId(id);
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