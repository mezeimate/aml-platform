package com.mezei.aml.common.tx;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
        UUID eventId,
        Instant eventTimestamp,
        String schemaVersion,

        String transactionId,
        Instant transactionTimestamp,

        String channel,
        String customerId,
        String accountId,
        String counterpartyAccount,
        String counterpartyName,

        BigDecimal amount,
        String currency,
        String direction,      // "DEBIT" / "CREDIT"

        String country,
        String originCountry,
        String destinationCountry,

        String mcc,
        String description
) {
}
