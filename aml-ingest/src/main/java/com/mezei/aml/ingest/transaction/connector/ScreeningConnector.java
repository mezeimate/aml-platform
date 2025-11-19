package com.mezei.aml.ingest.transaction.connector;

import com.mezei.aml.common.tx.TransactionEvent;

public interface ScreeningConnector {

    void sendToScreening(TransactionEvent event);

}
