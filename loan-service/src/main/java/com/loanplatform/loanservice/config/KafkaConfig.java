package com.loanplatform.loanservice.config;

import com.loanplatform.common.constants.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic loanEventsTopic() {
        return TopicBuilder.name(KafkaConstants.LOAN_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
