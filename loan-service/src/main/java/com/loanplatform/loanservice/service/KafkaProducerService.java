package com.loanplatform.loanservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanplatform.common.constants.KafkaConstants;
import com.loanplatform.common.event.LoanEvent;
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

    public void sendLoanEvent(LoanEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConstants.LOAN_EVENTS_TOPIC,
                    String.valueOf(event.getLoan().getId()), message);
            log.info("Sent loan event: {} for loan ID: {}", event.getEventType(), event.getLoan().getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize loan event", e);
        }
    }
}
