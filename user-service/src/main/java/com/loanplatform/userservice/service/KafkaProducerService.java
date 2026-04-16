package com.loanplatform.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanplatform.common.constants.KafkaConstants;
import com.loanplatform.common.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserEvent(UserEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConstants.USER_EVENTS_TOPIC, event.getUser().getEmail(), message);
            log.info("Sent user event: {} for user: {}", event.getEventType(), event.getUser().getEmail());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user event", e);
        }
    }
}
