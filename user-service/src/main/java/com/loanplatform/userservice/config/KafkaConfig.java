package com.loanplatform.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import com.loanplatform.common.constants.KafkaConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(KafkaConstants.USER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
