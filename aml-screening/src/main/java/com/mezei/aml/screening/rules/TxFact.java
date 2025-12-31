package com.mezei.aml.screening.rules;

import com.mezei.aml.common.tx.TransactionEvent;

import java.math.BigDecimal;

public record TxFact(TransactionEvent event) {

    public BigDecimal getAmount() {
        return event.amount();
    }

    public String getCustomerId() {
        return event.customerId();
    }

    public String getAccountId() {
        return event.accountId();
    }

    public String getTransactionId() {
        return event.transactionId();
    }

    public String getCurrency() { return event.currency(); }

    public String getOriginCountry() { return event.originCountry(); }

    public String getDestinationCountry() { return event.destinationCountry(); }
}
