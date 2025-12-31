package com.mezei.aml.screening.client;

import com.mezei.aml.common.alert.dto.CreateAlertRequest;
import com.mezei.aml.common.tx.TransactionEvent;
import com.mezei.aml.screening.model.ScreeningDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.toBodilessEntity()
                                .doOnSuccess(ignored ->
                                        log.info("Alert created for tx {}", event.transactionId()));
                    }

                    if (resp.statusCode() == HttpStatus.CONFLICT) {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .doOnNext(body -> log.info(
                                        "Alert already exists (409) for tx {} (dedupeKey={}). Body={}",
                                        event.transactionId(), decision.dedupeKey(), body))
                                .then(Mono.empty());
                    }

                    return resp.createException().flatMap(Mono::error);
                })
                .block(Duration.ofSeconds(5));
    }

    private CreateAlertRequest mapToRequest(TransactionEvent event, ScreeningDecision d) {
        String title = "Suspicious transaction " + event.transactionId();

        OffsetDateTime detectedAt = event.eventTimestamp() != null
                ? event.eventTimestamp().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.now(ZoneOffset.UTC);

        Map<String, Object> payload = Map.of(
                "transactionEvent", event,
                "screeningDecision", d
        );

        return new CreateAlertRequest(
                event.transactionId() + "|" + d.ruleId(),
                d.dedupeKey(),
                title,
                d.severity(),
                d.riskScore(),
                "SCREENING",
                d.ruleId(),
                d.ruleVersion(),
                event.customerId(),
                event.accountId(),
                detectedAt,
                payload,
                d.labels(),
                d.assignedTo()
        );
    }
}
