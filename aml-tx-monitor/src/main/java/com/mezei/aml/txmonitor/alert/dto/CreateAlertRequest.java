package com.mezei.aml.txmonitor.alert.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record CreateAlertRequest(
        @Size(max = 64)
        String externalId,

        @Size(max = 128)
        String dedupeKey,

        @NotBlank
        String title,

        @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL")
        String severity,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        BigDecimal riskScore,

        @NotBlank @Size(max = 64)
        String source,

        @Size(max = 64)
        String ruleId,

        @Size(max = 32)
        String ruleVersion,

        @Size(max = 64)
        String customerId,

        @Size(max = 64)
        String accountId,

        OffsetDateTime detectedAt,

        Map<String, Object> payload,

        List<@NotBlank String> labels,

        @Size(max = 128)
        String assignedTo
) {}
