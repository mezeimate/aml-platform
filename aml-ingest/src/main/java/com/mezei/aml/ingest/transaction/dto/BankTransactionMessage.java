package com.mezei.aml.ingest.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BankTransactionMessage(
        String transactionId,
        Instant transactionTimestamp,
        String channel,
        String customerId,
        String accountId,
        String counterpartyAccount,
        String counterpartyName,
        BigDecimal amount,
        String currency,
        String direction,
        String country,
        String originCountry,
        String destinationCountry,
        String mcc,
        String description
) {
}
