package com.mezei.aml.ingest.transaction.service;

import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.ingest.converter.TransactionMapper;
import com.mezei.aml.ingest.transaction.connector.ScreeningConnector;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionIngestService {

    private final ScreeningConnector screeningConnector;

    public void ingest(BankTransactionMessage msg) {
        log.info("TransactionIngestService received BankTransactionMessage, transactionId: {}", msg.transactionId());

        TransactionEvent event = TransactionMapper.transactionToEvent(msg);
        log.info("Mapped BankTransactionMessage to TransactionEvent, transactionId: {}", event.transactionId());

        screeningConnector.sendToScreening(event);
    }


}
