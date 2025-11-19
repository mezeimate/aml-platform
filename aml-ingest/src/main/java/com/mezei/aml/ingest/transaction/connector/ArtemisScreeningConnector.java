package com.mezei.aml.ingest.transaction.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.common.tx.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArtemisScreeningConnector implements ScreeningConnector {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aml.queues.screening-tx}")
    private String screeningQueue;

    @Override
    public void sendToScreening(TransactionEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("Sending TransactionEvent to '{}' queue: {}", screeningQueue, json);
            jmsTemplate.convertAndSend(screeningQueue, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TransactionEvent for screening", e);
        }
    }
}
