package com.kreview.kreview.service;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.Contract;
import com.kreview.kreview.ContractRequest;
import com.kreview.kreview.ContractType;
import com.kreview.kreview.PartyRole;
import com.kreview.kreview.exception.AnalysisInProgressException;
import com.kreview.kreview.exception.InvalidRequestException;
import com.kreview.kreview.exception.ResourceNotFoundException;
import com.kreview.kreview.jobs.JobStatusService;

import com.kreview.kreview.repository.AnalysisResultRepository;
import com.kreview.kreview.repository.ContractRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.kreview.kreview.messaging.ContractAnalysisPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContractService {

  private final ContractRepository contractRepository;
  private final AnalysisResultRepository analysisResultRepository;
  private final GeminiService geminiService;
  private final ContractAnalysisPublisher contractAnalysisPublisher;
  private final JobStatusService jobStatusService;
  private final DocumentTextExtractorService documentTextExtractorService;

  public ContractService(
      ContractRepository contractRepository,
      AnalysisResultRepository analysisResultRepository,
      GeminiService geminiService,
      ContractAnalysisPublisher contractAnalysisPublisher,
      JobStatusService jobStatusService,
      DocumentTextExtractorService documentTextExtractorService) {
    this.contractRepository = contractRepository;
    this.analysisResultRepository = analysisResultRepository;
    this.geminiService = geminiService;
    this.contractAnalysisPublisher = contractAnalysisPublisher;
    this.jobStatusService = jobStatusService;
    this.documentTextExtractorService = documentTextExtractorService;
  }

  public List<Contract> getAllContracts() {
    return contractRepository.findAll();
  }

  public Contract submitContract(ContractRequest request) {
    return saveContract(request.getContent(), request.getContractType(), request.getPartyRole(),
        request.getPartyRoleCustomLabel());
  }

  public Contract submitContractFromFile(MultipartFile file, ContractType type, PartyRole role,
                                          String customLabel) {
    String content = documentTextExtractorService.extractText(file);
    return saveContract(content, type, role, customLabel);
  }

  private Contract saveContract(String content, ContractType type, PartyRole role,
                                 String customLabel) {
    if (content == null || content.isBlank()) {
      throw new InvalidRequestException("Contract content must not be blank");
    }

    validateTypeAndRole(type, role, customLabel);

    Contract contract = new Contract();
    contract.setContent(content);
    contract.setCreatedAt(LocalDateTime.now());
    contract.setContractType(type);
    contract.setPartyRole(role);
    contract.setPartyRoleCustomLabel(role == PartyRole.OTHER ? customLabel : null);

    return contractRepository.save(contract);
  }

  private void validateTypeAndRole(ContractType type, PartyRole role, String customLabel) {
    if (type != null && role == null) {
      throw new InvalidRequestException("partyRole is required when contractType is specified");
    }

    if (role == null) {
      return;
    }

    Set<PartyRole> validRoles = type != null ? type.validRoles() : PartyRole.GENERIC_ROLES;
    if (!validRoles.contains(role)) {
      throw new InvalidRequestException(
          "Party role " + role + " is not valid for contract type "
              + (type != null ? type : "GENERIC"));
    }

    if (role == PartyRole.OTHER && (customLabel == null || customLabel.isBlank())) {
      throw new InvalidRequestException(
          "partyRoleCustomLabel must be provided when partyRole is OTHER");
    }
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