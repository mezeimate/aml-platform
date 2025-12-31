package com.mezei.aml.screening.service;

import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.screening.client.AlertClient;
import com.mezei.aml.screening.model.ScreeningDecision;
import com.mezei.aml.screening.rules.RuleEngine;
import com.mezei.aml.screening.rules.RuleHit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreeningService {

    private final AlertClient alertClient;
    private final RuleEngine ruleEngine;

    public void evaluate(TransactionEvent event) {
        List<RuleHit> hits = ruleEngine.evaluate(event);
        ScreeningDecision decision = toDecision(event, hits);

        if (!decision.createAlert()) {
            log.info("Not suspicious. hits={}", hits.size());
            return;
        }

        log.info("Suspicious transaction. hits={}, topRule={}", hits.size(), decision.ruleId());

        try {
            alertClient.createAlert(event, decision);
        } catch (Exception e) {
            log.error("Failed to create alert for txId={}, ruleId={}.", event.transactionId(), decision.ruleId(), e);
        }
    }

    private ScreeningDecision toDecision(TransactionEvent event, List<RuleHit> hits) {
        if (hits.isEmpty()) {
            return new ScreeningDecision(false, null, null, null, null, null, List.of(), "No rule triggered", null);
        }

        RuleHit top = hits.getFirst();

        var allLabels = hits.stream().flatMap(h -> h.labels().stream()).distinct().toList();
        String explanation = hits.stream()
                .map(h -> h.ruleId() + ": " + (h.explanation() == null ? "" : h.explanation()))
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("Rule triggered");

        return new ScreeningDecision(
                true,
                top.ruleId(),
                top.ruleVersion(),
                top.severity(),
                top.riskScore(),
                top.dedupeKey(),
                allLabels,
                explanation,
                null
        );
    }
}
