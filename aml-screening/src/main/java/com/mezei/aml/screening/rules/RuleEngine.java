package com.mezei.aml.screening.rules;

import com.mezei.aml.common.tx.TransactionEvent;

import java.util.List;

public interface RuleEngine {
    List<RuleHit> evaluate(TransactionEvent event);
}
