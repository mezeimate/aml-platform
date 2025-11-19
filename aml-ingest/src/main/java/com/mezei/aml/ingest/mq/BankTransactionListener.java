package com.mezei.aml.ingest.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.ingest.transaction.service.TransactionIngestService;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankTransactionListener {

    private final ObjectMapper objectMapper;
    private final TransactionIngestService transactionIngestService;

    @Value("${aml.queues.bank-tx-in}")
    private String bankQueue;

    @JmsListener(destination = "${aml.queues.bank-tx-in}")
    public void onMessage(String json) {
        try {
            log.info("Received raw JSON from '{}': {}", bankQueue, json);

            BankTransactionMessage msg = objectMapper.readValue(json, BankTransactionMessage.class);

            log.info("Parsed BankTransactionMessage: {}", msg);

            // uzleti retegbe
            transactionIngestService.ingest(msg);

        } catch (Exception e) {
            log.error("Error processing message from '{}'", bankQueue, e);
            // DLQ / retry ...
        }
    }
}
