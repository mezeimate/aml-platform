package com.mezei.aml.txmonitor.alert.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mezei.aml.jooq.enums.AlertSeverity;
import com.mezei.aml.jooq.enums.AlertStatus;
import com.mezei.aml.jooq.tables.records.AlertRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mezei.aml.jooq.tables.Alert.ALERT;

@Repository
public class AlertRepository {

    private final DSLContext dsl;

    public AlertRepository(DSLContext dsl, ObjectMapper om) {
        this.dsl = dsl;
    }

    public AlertRecord newRecord() {
        return dsl.newRecord(ALERT);
    }

    public AlertRecord insert(AlertRecord record) {
        if (record.configuration() == null) {
            record.attach(dsl.configuration());
        }

        if (record.getRiskScore() == null) {
            record.setRiskScore(new BigDecimal("0.0"));
        }

        record.store();
        return record;
    }

    public Optional<AlertRecord> findById(UUID id) {
        AlertRecord r = dsl.selectFrom(ALERT)
                .where(ALERT.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(r);
    }

    public List<AlertRecord> list(int limit, int offset) {
        return dsl.selectFrom(ALERT)
                .orderBy(ALERT.DETECTED_AT.desc().nullsLast(), ALERT.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }

    public int updateDetails(UUID id, AlertRecord req) {
        BigDecimal risk = (req.getRiskScore() == null)
                ? new BigDecimal("0.0")
                : req.getRiskScore();

        return dsl.update(ALERT)
                .set(ALERT.EXTERNAL_ID, req.getExternalId())
                .set(ALERT.DEDUPE_KEY, req.getDedupeKey())
                .set(ALERT.TITLE,        req.getTitle())
                .set(ALERT.SEVERITY,     req.getSeverity())
                .set(ALERT.RISK_SCORE,   risk)
                .set(ALERT.SOURCE,       req.getSource())
                .set(ALERT.RULE_ID,      req.getRuleId())
                .set(ALERT.RULE_VERSION, req.getRuleVersion())
                .set(ALERT.CUSTOMER_ID,  req.getCustomerId())
                .set(ALERT.ACCOUNT_ID,   req.getAccountId())
                .set(ALERT.DETECTED_AT,  req.getDetectedAt())
                .set(ALERT.PAYLOAD,      req.getPayload())
                .set(ALERT.LABELS,       req.getLabels())
                .set(ALERT.ASSIGNED_TO,  req.getAssignedTo())
                .where(ALERT.ID.eq(id))
                .execute();
    }

    public int updateStatus(UUID id, AlertStatus newStatus, String assignedTo) {
        if (assignedTo != null && !assignedTo.isBlank()) {
            return dsl.update(ALERT)
                    .set(ALERT.STATUS, newStatus)
                    .set(ALERT.ASSIGNED_TO, assignedTo)
                    .where(ALERT.ID.eq(id))
                    .execute();
        } else {
            return dsl.update(ALERT)
                    .set(ALERT.STATUS, newStatus)
                    .set(ALERT.ASSIGNED_TO, (String) null)
                    .where(ALERT.ID.eq(id))
                    .execute();
        }
    }

    public int delete(UUID id) {
        return dsl.deleteFrom(ALERT)
                .where(ALERT.ID.eq(id))
                .execute();
    }

    public List<AlertRecord> search(String status, String severity, String label, String customerId,
            OffsetDateTime from, OffsetDateTime to, int limit, int offset) {
        Condition condition = DSL.noCondition();

        if (status != null && !status.isBlank()) {
            condition = condition.and(ALERT.STATUS.eq(AlertStatus.valueOf(status)));
        }

        if (severity != null && !severity.isBlank()) {
            condition = condition.and(ALERT.SEVERITY.eq(AlertSeverity.valueOf(severity)));
        }

        if (label != null && !label.isBlank()) {
            condition = condition.and(ALERT.LABELS.contains(new String[]{label}));
        }

        if (customerId != null && !customerId.isBlank()) {
            condition = condition.and(ALERT.CUSTOMER_ID.eq(customerId));
        }

        if (from != null) {
            condition = condition.and(ALERT.DETECTED_AT.ge(from));
        }

        if (to != null) {
            condition = condition.and(ALERT.DETECTED_AT.le(to));
        }

        return dsl.selectFrom(ALERT)
                .where(condition)
                .orderBy(ALERT.DETECTED_AT.desc().nullsLast(), ALERT.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }
}