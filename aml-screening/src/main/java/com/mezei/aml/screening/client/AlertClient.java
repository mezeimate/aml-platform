package com.mezei.aml.screening.client;

import com.mezei.aml.common.alert.dto.CreateAlertRequest;
import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.screening.model.ScreeningDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertClient {

    private final WebClient alertWebClient;

    public void createAlert(TransactionEvent event, ScreeningDecision decision) {
        CreateAlertRequest request = mapToRequest(event, decision);

        log.info("Calling aml-tx-monitor to create alert for tx {} ...", event.transactionId());

        alertWebClient.post()
                .uri("/api/v1/alerts")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block(); // MVP-ben elmegy

        log.info("Alert create request sent for tx {}", event.transactionId());
    }

    private CreateAlertRequest mapToRequest(TransactionEvent event, ScreeningDecision d) {
        String title = "Suspicious transaction " + event.transactionId();

        OffsetDateTime detectedAt = event.eventTimestamp() != null
                ? event.eventTimestamp().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.now(ZoneOffset.UTC);

        // payload: TransactionEvent + ScreeningDecision együtt, JSONB-be jó lesz
        Map<String, Object> payload = Map.of(
                "transactionEvent", event,
                "screeningDecision", d
        );

        return new CreateAlertRequest(
                event.transactionId(),      // externalId
                d.dedupeKey(),              // dedupeKey
                title,                      // title
                d.severity(),               // severity
                d.riskScore(),              // riskScore (BigDecimal)
                "SCREENING",                // source
                d.ruleId(),                 // ruleId
                d.ruleVersion(),            // ruleVersion
                event.customerId(),         // customerId
                event.accountId(),          // accountId
                detectedAt,                 // detectedAt (OffsetDateTime)
                payload,                    // payload (Map<String,Object>)
                d.labels(),                 // labels
                d.assignedTo()              // assignedTo
        );
    }
}
