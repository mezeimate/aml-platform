package com.mezei.aml.ingest.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.ingest.transaction.service.TransactionIngestService;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import jakarta.validation.Validator;
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
    private final Validator validator;
    private final ArtemisDlqProducer dlqProducer;

    @Value("${aml.queues.bank-tx-in}")
    private String bankQueue;

    @JmsListener(destination = "${aml.queues.bank-tx-in}")
    public void onMessage(String json) {
        try {
            log.info("Received raw JSON from '{}': {}", bankQueue, json);

            BankTransactionMessage msg = objectMapper.readValue(json, BankTransactionMessage.class);
            if (!validator.validate(msg).isEmpty()) {
                log.error("Validation failed, message sending to DLQ.");
                dlqProducer.sendToDlq(new DlqError("VALIDATION_ERROR", "VALIDATION_ERROR", json));
                return;
            }
            log.info("Parsed & validated BankTransactionMessage: {}", msg);
            transactionIngestService.ingest(msg);

        } catch (JsonProcessingException e) {
            log.error("JSON parse error, sending to DLQ: {}", json, e);
            dlqProducer.sendToDlq(new DlqError("JSON_PARSE_ERROR", e.getMessage(), json));

        } catch (Exception e) {
            log.error("Unexpected ingest error, sending to DLQ: {}", json, e);
            dlqProducer.sendToDlq(new DlqError("UNKNOWN_ERROR", e.getMessage(), json));
        }
    }
}
