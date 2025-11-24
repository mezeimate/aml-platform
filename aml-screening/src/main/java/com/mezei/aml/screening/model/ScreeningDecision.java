package com.mezei.aml.screening.model;

import java.math.BigDecimal;
import java.util.List;

public record ScreeningDecision(
        boolean createAlert,
        String ruleId,
        String ruleVersion,
        String severity,
        BigDecimal riskScore,
        String dedupeKey,
        List<String> labels,
        String explanation,
        String assignedTo
) { }
