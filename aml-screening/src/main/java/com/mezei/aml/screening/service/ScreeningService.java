package com.mezei.aml.screening.service;

import com.mezei.aml.common.tx.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

    public void evaluate(TransactionEvent event) {

        log.info("Evaluating TransactionEvent...");

        // szabalyok
        boolean suspicious = dummyRule(event);

        if (suspicious) {
            log.info("Transaction suspicious → ALERT should be created");
            // call aml-tx-monitor http
        } else {
            log.info("✔ Transaction NOT suspicious");
        }
    }

    private boolean dummyRule(TransactionEvent event) {
        return event.amount().longValue() > 150_000;
    }
}
