package com.mezei.aml.ingest.converter;

import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.UUID;

@UtilityClass
public class TransactionMapper {

    public static TransactionEvent transactionToEvent(BankTransactionMessage r) {
        return new TransactionEvent(
                UUID.randomUUID(),          // eventId
                Instant.now(),              // eventTimestamp
                "1.0",                      // schemaVersion

                r.transactionId(),
                r.transactionTimestamp(),

                r.channel(),
                r.customerId(),
                r.accountId(),
                r.counterpartyAccount(),
                r.counterpartyName(),

                r.amount(),
                r.currency(),
                r.direction(),

                r.country(),
                r.originCountry(),
                r.destinationCountry(),

                r.mcc(),
                r.description()
        );
    }
}
