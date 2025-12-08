package com.mezei.aml.screening.service;

import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.screening.client.AlertClient;
import com.mezei.aml.screening.model.ScreeningDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreeningService {

    private final AlertClient alertClient;

    public void evaluate(TransactionEvent event) {
        log.info("Evaluating TransactionEvent in ScreeningService...");

        ScreeningDecision decision = evaluateRules(event);

        if (decision.createAlert()) {
            log.info("Transaction marked suspicious, creating alert...");
            alertClient.createAlert(event, decision);
        } else {
            log.info("Transaction NOT suspicious, no alert.");
        }
    }

    private ScreeningDecision evaluateRules(TransactionEvent event) {
        boolean suspicious = event.amount() != null
                && event.amount().longValue() > 150_000;

        if (!suspicious) {
            return new ScreeningDecision(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    "No rule triggered",
                    null
            );
        }

        String ruleId = "R001_HIGH_AMOUNT";
        String ruleVersion = "1.0";
        String severity = "HIGH";
        BigDecimal riskScore = BigDecimal.valueOf(80);

        String dedupeKey = ruleId + "|" + event.customerId() + "|" + event.accountId();
        var labels = List.of("HIGH_AMOUNT", "DUMMY_RULE");
        String explanation = "Amount above 150_000 threshold";
        String assignedTo = null;

        return new ScreeningDecision(
                true,
                ruleId,
                ruleVersion,
                severity,
                riskScore,
                dedupeKey,
                labels,
                explanation,
                assignedTo
        );
    }
}
