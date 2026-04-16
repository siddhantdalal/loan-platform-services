package com.loanplatform.loanservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanplatform.common.constants.KafkaConstants;
import com.loanplatform.common.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConstants.USER_EVENTS_TOPIC, groupId = KafkaConstants.LOAN_GROUP)
    public void consumeUserEvent(String message) {
        try {
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            log.info("Received user event: {} for user: {}", event.getEventType(), event.getUser().getEmail());

            if (UserEvent.REGISTERED.equals(event.getEventType())) {
                log.info("New user registered: {} {} ({})",
                        event.getUser().getFirstName(),
                        event.getUser().getLastName(),
                        event.getUser().getEmail());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user event: {}", message, e);
        }
    }
}
