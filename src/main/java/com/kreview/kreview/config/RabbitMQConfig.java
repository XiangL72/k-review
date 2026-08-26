package com.kreview.kreview.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String CONTRACT_ANALYSIS_QUEUE = "contract.analysis.queue";
  public static final String CONTRACT_ANALYSIS_DLX = "contract.analysis.dlx";
  public static final String CONTRACT_ANALYSIS_DLQ = "contract.analysis.dlq";

  @Bean
  public Queue contractAnalysisQueue() {
    return QueueBuilder.durable(CONTRACT_ANALYSIS_QUEUE)
        .withArgument("x-dead-letter-exchange", CONTRACT_ANALYSIS_DLX)
        .build();
  }

  @Bean
  public DirectExchange contractAnalysisDlx() {
    return new DirectExchange(CONTRACT_ANALYSIS_DLX);
  }

  @Bean
  public Queue contractAnalysisDlq() {
    return new Queue(CONTRACT_ANALYSIS_DLQ, true);
  }

  @Bean
  public Binding contractAnalysisDlqBinding(Queue contractAnalysisDlq, DirectExchange contractAnalysisDlx) {
    return BindingBuilder.bind(contractAnalysisDlq).to(contractAnalysisDlx).with(CONTRACT_ANALYSIS_QUEUE);
  }

  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       MessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }

  @Bean
  public ApplicationRunner declareQueues(RabbitAdmin rabbitAdmin,
                                          Queue contractAnalysisQueue,
                                          DirectExchange contractAnalysisDlx,
                                          Queue contractAnalysisDlq,
                                          Binding contractAnalysisDlqBinding) {
    return args -> {
      try {
        rabbitAdmin.declareExchange(contractAnalysisDlx);
        rabbitAdmin.declareQueue(contractAnalysisDlq);
        rabbitAdmin.declareBinding(contractAnalysisDlqBinding);
        rabbitAdmin.declareQueue(contractAnalysisQueue);
        System.out.println("✓ Declared queue: " + contractAnalysisQueue.getName()
            + " (dead-lettering to " + contractAnalysisDlq.getName() + ")");
      } catch (Exception e) {
        System.err.println(
            "⚠ Could not declare queue at startup (RabbitMQ may be down): " + e.getMessage());
        System.err.println(
            "  The app will continue starting. Publishing will fail until RabbitMQ is reachable.");
      }
    };
  }
}