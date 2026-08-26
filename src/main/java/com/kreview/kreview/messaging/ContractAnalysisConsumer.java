package com.kreview.kreview.messaging;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.Contract;
import com.kreview.kreview.repository.ContractRepository;
import com.kreview.kreview.service.ContractService;
import com.kreview.kreview.service.GeminiService;
import com.kreview.kreview.config.RabbitMQConfig;
import com.kreview.kreview.jobs.JobStatusService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ContractAnalysisConsumer {

  private final JobStatusService jobStatusService;
  private final ContractRepository contractRepository;
  private final GeminiService geminiService;
  private final ContractService contractService;

  public ContractAnalysisConsumer(
      JobStatusService jobStatusService,
      ContractRepository contractRepository,
      GeminiService geminiService,
      ContractService contractService) {
    this.jobStatusService = jobStatusService;
    this.contractRepository = contractRepository;
    this.geminiService = geminiService;
    this.contractService = contractService;
  }

  @RabbitListener(queues = RabbitMQConfig.CONTRACT_ANALYSIS_QUEUE)
  public void handleAnalysisJob(AnalysisJobMessage message) {
    String jobId = message.getJobId();
    Long contractId = message.getContractId();

    System.out.println("📥 Worker starting job " + jobId + " for contract " + contractId);

    try {
      jobStatusService.setStatus(jobId, "PROCESSING");

      Contract contract = contractRepository.findById(contractId).orElse(null);
      if (contract == null) {
        throw new RuntimeException("Contract not found: " + contractId);
      }

      String aiResponse = geminiService.analyzeContractWithAI(contract.getContent());
      AnalysisResult result = geminiService.parseAnalysisResponse(aiResponse, contract);
      contractService.replaceAnalysisResult(contractId, result);

      jobStatusService.setStatus(jobId, "COMPLETE");
      System.out.println("✅ Worker completed job " + jobId);

    } catch (Exception e) {
      jobStatusService.setStatus(jobId, "FAILED");
      System.err.println("❌ Worker failed job " + jobId + ": " + e.getMessage());
    } finally {
      jobStatusService.clearActive(contractId);
    }
  }
}