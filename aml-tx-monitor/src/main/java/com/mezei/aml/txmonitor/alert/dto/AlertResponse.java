package com.mezei.aml.txmonitor.alert.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String externalId,
        String dedupeKey,
        String title,
        String status,
        String severity,
        BigDecimal riskScore,
        String source,
        String ruleId,
        String ruleVersion,
        String customerId,
        String accountId,
        OffsetDateTime detectedAt,
        Map<String, Object> payload,
        List<String> labels,
        String assignedTo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
