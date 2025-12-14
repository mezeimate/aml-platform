package com.mezei.aml.ingest.mq;

import com.mezei.aml.common.constants.AmlConstants;

import com.mezei.aml.ingest.converter.Iso20022Parser;
import com.mezei.aml.ingest.exception.IsoParseException;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import com.mezei.aml.ingest.transaction.service.TransactionIngestService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Iso20022TransactionListener {

    private final Iso20022Parser iso20022Parser;
    private final TransactionIngestService transactionIngestService;
    private final Validator validator;
    private final ArtemisDlqProducer dlqProducer;

    @Value("${aml.queues.iso20022-tx-in}")
    private String isoQueue;

    @JmsListener(destination = "${aml.queues.iso20022-tx-in}")
    public void onMessage(String xml) {
        try {
            log.info("Received ISO20022 XML from '{}'", isoQueue);

            BankTransactionMessage msg = iso20022Parser.parseToInternal(xml);

            if (!validator.validate(msg).isEmpty()) {
                log.error("Validation failed for ISO20022 message, sending to DLQ.");
                dlqProducer.sendToDlq(new DlqError(AmlConstants.ERROR_CODE_VALIDATION, AmlConstants.ERROR_CODE_VALIDATION, xml));
                return;
            }

            log.info("Parsed, mapped & validated ISO20022 BankTransactionMessage, transactionId: {}", msg.transactionId());
            transactionIngestService.ingest(msg);

        } catch (IsoParseException e) {
            log.error("ISO20022 parse error, sending to DLQ: {}", xml, e);
            dlqProducer.sendToDlq(new DlqError(AmlConstants.ERROR_CODE_XML_PARSE, e.getMessage(), xml));

        } catch (Exception e) {
            log.error("Unexpected ISO ingest error, sending to DLQ: {}", xml, e);
            dlqProducer.sendToDlq(new DlqError(AmlConstants.ERROR_CODE_UNKNOWN, e.getMessage(), xml));
        }
    }
}
