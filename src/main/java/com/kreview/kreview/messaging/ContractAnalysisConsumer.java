package com.kreview.kreview.messaging;

import com.kreview.kreview.AnalysisResult;
import com.kreview.kreview.AnalysisResultRepository;
import com.kreview.kreview.Contract;
import com.kreview.kreview.ContractRepository;
import com.kreview.kreview.GeminiService;
import com.kreview.kreview.config.RabbitMQConfig;
import com.kreview.kreview.jobs.JobStatusService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ContractAnalysisConsumer {

  private final JobStatusService jobStatusService;
  private final ContractRepository contractRepository;
  private final GeminiService geminiService;
  private final AnalysisResultRepository analysisResultRepository;

  public ContractAnalysisConsumer(
      JobStatusService jobStatusService,
      ContractRepository contractRepository,
      GeminiService geminiService,
      AnalysisResultRepository analysisResultRepository) {
    this.jobStatusService = jobStatusService;
    this.contractRepository = contractRepository;
    this.geminiService = geminiService;
    this.analysisResultRepository = analysisResultRepository;
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

      AnalysisResult existing = analysisResultRepository.findByContractId(contractId);
      if (existing != null) {
        analysisResultRepository.delete(existing);
      }

      String aiResponse = geminiService.analyzeContractWithAI(contract.getContent());
      AnalysisResult result = geminiService.parseAnalysisResponse(aiResponse, contract);
      analysisResultRepository.save(result);

      jobStatusService.setStatus(jobId, "COMPLETE");
      System.out.println("✅ Worker completed job " + jobId);

    } catch (Exception e) {
      jobStatusService.setStatus(jobId, "FAILED");
      System.err.println("❌ Worker failed job " + jobId + ": " + e.getMessage());
    }
  }
}