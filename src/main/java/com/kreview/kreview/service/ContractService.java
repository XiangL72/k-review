package com.kreview.kreview.service;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.Contract;
import com.kreview.kreview.ContractRequest;
import com.kreview.kreview.exception.AnalysisInProgressException;
import com.kreview.kreview.exception.ResourceNotFoundException;
import com.kreview.kreview.jobs.JobStatusService;

import com.kreview.kreview.repository.AnalysisResultRepository;
import com.kreview.kreview.repository.ContractRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.kreview.kreview.messaging.ContractAnalysisPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractService {

  private final ContractRepository contractRepository;
  private final AnalysisResultRepository analysisResultRepository;
  private final GeminiService geminiService;
  private final ContractAnalysisPublisher contractAnalysisPublisher;
  private final JobStatusService jobStatusService;

  public ContractService(
      ContractRepository contractRepository,
      AnalysisResultRepository analysisResultRepository,
      GeminiService geminiService,
      ContractAnalysisPublisher contractAnalysisPublisher,
      JobStatusService jobStatusService) {
    this.contractRepository = contractRepository;
    this.analysisResultRepository = analysisResultRepository;
    this.geminiService = geminiService;
    this.contractAnalysisPublisher = contractAnalysisPublisher;
    this.jobStatusService = jobStatusService;
  }

  public List<Contract> getAllContracts() {
    return contractRepository.findAll();
  }

  public Contract submitContract(ContractRequest request) {
    Contract contract = new Contract();
    contract.setContent(request.getContent());
    contract.setCreatedAt(LocalDateTime.now());

    return contractRepository.save(contract);
  }

  public List<Contract> searchContracts(String query) {
    return contractRepository.searchByText(query);
  }

  public Contract getContractById(Long id) {
    return contractRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
  }

  public String submitAnalysisJob(Contract contract) {
    String jobId = java.util.UUID.randomUUID().toString();

    if (!jobStatusService.tryMarkActive(contract.getId(), jobId)) {
      throw new AnalysisInProgressException(
          "Analysis already in progress for contract: " + contract.getId());
    }

    jobStatusService.setStatus(jobId, "PENDING");

    contractAnalysisPublisher.publishAnalysisJob(contract.getId(), jobId);

    return jobId;
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

  public AnalysisResult getAnalysisByContractId(Long contractId) {
    AnalysisResult result = analysisResultRepository.findByContractId(contractId);
    if (result == null) {
      throw new ResourceNotFoundException("No analysis found for contract: " + contractId);
    }
    return result;
  }

  @Transactional
  public void replaceAnalysisResult(Long contractId, AnalysisResult newResult) {
    AnalysisResult existing = analysisResultRepository.findByContractId(contractId);
    if (existing != null) {
      analysisResultRepository.delete(existing);
    }
    analysisResultRepository.save(newResult);
  }
}