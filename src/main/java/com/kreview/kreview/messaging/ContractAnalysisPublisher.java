package com.kreview.kreview.messaging;

import com.kreview.kreview.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ContractAnalysisPublisher {

  private final RabbitTemplate rabbitTemplate;

  public ContractAnalysisPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishAnalysisJob(Long contractId, String jobId) {
    AnalysisJobMessage message = new AnalysisJobMessage(contractId, jobId);
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.CONTRACT_ANALYSIS_QUEUE,
        message
    );
  }
}