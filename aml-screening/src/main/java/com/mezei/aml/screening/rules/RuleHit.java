package com.mezei.aml.screening.rules;

import java.math.BigDecimal;
import java.util.List;

public record RuleHit(
        String ruleId,
        String ruleVersion,
        String severity,
        BigDecimal riskScore,
        String dedupeKey,
        List<String> labels,
        String explanation
) {}
