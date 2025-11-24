package com.mezei.aml.ingest.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArtemisDlqProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aml.queues.bank-tx-in-dlq}")
    private String dlqQueue;

    public void sendToDlq(Object dlqPayload) {
        try {
            String json = objectMapper.writeValueAsString(dlqPayload);
            jmsTemplate.convertAndSend(dlqQueue, json);
            log.error("Sent message to DLQ '{}': {}", dlqQueue, json);
        } catch (Exception e) {
            log.error("FAILED to send message to DLQ '{}': {}", dlqQueue, dlqPayload, e);
        }
    }
}
