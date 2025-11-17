package com.mezei.aml.txmonitor.alert.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.jooq.enums.AlertSeverity;
import com.mezei.aml.jooq.enums.AlertStatus;
import com.mezei.aml.jooq.tables.records.AlertRecord;
import com.mezei.aml.txmonitor.alert.dto.AlertResponse;
import com.mezei.aml.txmonitor.alert.dto.CreateAlertRequest;
import com.mezei.aml.txmonitor.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.JSONB;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AlertResponse createAlert(CreateAlertRequest request) {
        AlertRecord rec = alertRepository.newRecord();

        rec.setExternalId(request.externalId());
        rec.setDedupeKey(request.dedupeKey());
        rec.setTitle(request.title());

        if (request.severity() != null && !request.severity().isBlank()) {
            rec.setSeverity(AlertSeverity.valueOf(request.severity()));
        }

        BigDecimal risk = request.riskScore() == null
                ? new BigDecimal("0.0")
                : request.riskScore();
        rec.setRiskScore(risk);

        rec.setSource(request.source());
        rec.setRuleId(request.ruleId());
        rec.setRuleVersion(request.ruleVersion());
        rec.setCustomerId(request.customerId());
        rec.setAccountId(request.accountId());
        rec.setDetectedAt(request.detectedAt());

        rec.setPayload(JSONB.valueOf(writeJson(request.payload())));
        rec.setLabels(request.labels() == null
                ? new String[] {}
                : request.labels().toArray(new String[0]));

        rec.setAssignedTo(request.assignedTo());

        AlertRecord saved = alertRepository.insert(rec);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<AlertResponse> getAlertById(UUID id) {
        return alertRepository.findById(id)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> listAlerts(int limit, int offset) {
        return alertRepository.list(limit, offset)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public boolean updateAlertDetails(UUID id, CreateAlertRequest request) {
        AlertRecord rec = alertRepository.newRecord();

        rec.setExternalId(request.externalId());
        rec.setDedupeKey(request.dedupeKey());
        rec.setTitle(request.title());

        if (request.severity() != null && !request.severity().isBlank()) {
            rec.setSeverity(AlertSeverity.valueOf(request.severity()));
        } else {
            rec.setSeverity(null);
        }

        BigDecimal risk = request.riskScore() == null
                ? new BigDecimal("0.0")
                : request.riskScore();
        rec.setRiskScore(risk);

        rec.setSource(request.source());
        rec.setRuleId(request.ruleId());
        rec.setRuleVersion(request.ruleVersion());
        rec.setCustomerId(request.customerId());
        rec.setAccountId(request.accountId());
        rec.setDetectedAt(request.detectedAt());

        rec.setPayload(JSONB.valueOf(writeJson(request.payload())));
        rec.setLabels(request.labels() == null
                ? new String[] {}
                : request.labels().toArray(new String[0]));

        rec.setAssignedTo(request.assignedTo());

        int updatedRows = alertRepository.updateDetails(id, rec);
        return updatedRows > 0;
    }

    @Transactional
    public boolean updateAlertStatus(UUID id, String newStatus, String assignedTo) {
        AlertStatus status = AlertStatus.valueOf(newStatus);
        int updatedRows = alertRepository.updateStatus(id, status, assignedTo);
        return updatedRows > 0;
    }

    @Transactional
    public boolean deleteAlert(UUID id) {
        int deletedRows = alertRepository.delete(id);
        return deletedRows > 0;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> searchAlerts(String status, String severity, String label, String customerId,
            OffsetDateTime from, OffsetDateTime to, int limit, int offset) {
        return alertRepository.search(status, severity, label, customerId, from, to, limit, offset)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AlertResponse toDto(AlertRecord r) {
        return new AlertResponse(
                r.getId(),
                r.getExternalId(),
                r.getDedupeKey(),
                r.getTitle(),
                r.getStatus() == null ? null : r.getStatus().name(),
                r.getSeverity() == null ? null : r.getSeverity().name(),
                r.getRiskScore(),
                r.getSource(),
                r.getRuleId(),
                r.getRuleVersion(),
                r.getCustomerId(),
                r.getAccountId(),
                r.getDetectedAt(),
                readJson(r.getPayload()),
                r.getLabels() == null ? List.of() : List.of(r.getLabels()),
                r.getAssignedTo(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private Map<String, Object> readJson(JSONB jsonb) {
        try {
            if (jsonb == null) {
                return Map.of();
            }
            return objectMapper.readValue(jsonb.data(), new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String writeJson(Map<String, Object> map) {
        try {
            Map<String, Object> safe = (map == null) ? Map.of() : map;
            return objectMapper.writeValueAsString(safe);
        } catch (Exception e) {
            return "{}";
        }
    }
}
