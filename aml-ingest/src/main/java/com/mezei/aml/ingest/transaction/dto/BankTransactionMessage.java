package com.mezei.aml.ingest.transaction.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record BankTransactionMessage(

        @NotBlank
        @Size(max = 64)
        String transactionId,

        @NotNull
        @PastOrPresent
        Instant transactionTimestamp,

        @NotBlank
        @Size(max = 32)
        String channel,

        @NotBlank
        @Size(max = 64)
        String customerId,

        @NotBlank
        @Size(max = 64)
        String accountId,

        @NotBlank
        @Size(max = 34)
        String counterpartyAccount,

        @NotBlank
        @Size(max = 128)
        String counterpartyName,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotBlank
        @Pattern(regexp = "DEBIT|CREDIT")
        String direction,

        @NotBlank
        @Size(min = 2, max = 2)
        String country,

        @NotBlank
        @Size(min = 2, max = 2)
        String originCountry,

        @NotBlank
        @Size(min = 2, max = 2)
        String destinationCountry,

        @Size(max = 4)
        String mcc,

        @Size(max = 512)
        String description
) { }
