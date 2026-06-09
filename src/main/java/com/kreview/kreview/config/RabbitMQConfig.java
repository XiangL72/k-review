package com.kreview.kreview.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String CONTRACT_ANALYSIS_QUEUE = "contract.analysis.queue";

  @Bean
  public Queue contractAnalysisQueue() {
    return new Queue(CONTRACT_ANALYSIS_QUEUE, true);
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public ApplicationRunner declareQueues(RabbitAdmin rabbitAdmin, Queue contractAnalysisQueue) {
    return args -> {
      try {
        rabbitAdmin.declareQueue(contractAnalysisQueue);
        System.out.println("✓ Declared queue: " + contractAnalysisQueue.getName());
      } catch (Exception e) {
        System.err.println(
            "⚠ Could not declare queue at startup (RabbitMQ may be down): " + e.getMessage());
        System.err.println(
            "  The app will continue starting. Publishing will fail until RabbitMQ is reachable.");
      }
    };
  }
}