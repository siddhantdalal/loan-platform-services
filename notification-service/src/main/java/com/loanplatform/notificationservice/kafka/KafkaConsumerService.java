package com.loanplatform.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanplatform.common.constants.KafkaConstants;
import com.loanplatform.common.event.LoanEvent;
import com.loanplatform.common.event.UserEvent;
import com.loanplatform.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaConstants.USER_EVENTS_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void consumeUserEvent(String message) {
        try {
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            log.info("Received user event: {} for user: {}", event.getEventType(), event.getUser().getEmail());
            notificationService.processUserEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user event: {}", message, e);
        }
    }

    @KafkaListener(topics = KafkaConstants.LOAN_EVENTS_TOPIC, groupId = KafkaConstants.NOTIFICATION_GROUP)
    public void consumeLoanEvent(String message) {
        try {
            LoanEvent event = objectMapper.readValue(message, LoanEvent.class);
            log.info("Received loan event: {} for loan ID: {}", event.getEventType(), event.getLoan().getId());
            notificationService.processLoanEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize loan event: {}", message, e);
        }
    }
}
