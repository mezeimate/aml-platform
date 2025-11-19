package com.mezei.aml.screening.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.screening.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScreeningListener {

    private final ObjectMapper objectMapper;
    private final ScreeningService screeningService;

    @Value("${aml.queues.screening-tx}")
    private String screeningQueue;

    @JmsListener(destination = "${aml.queues.screening-tx}")
    public void onMessage(String json) {
        try {
            TransactionEvent event = objectMapper.readValue(json, TransactionEvent.class);

            log.info("ScreeningListener received TransactionEvent: {}", event);

            screeningService.evaluate(event);

        } catch (Exception e) {
            log.error("Error reading from '{}'", screeningQueue, e);
        }
    }
}
