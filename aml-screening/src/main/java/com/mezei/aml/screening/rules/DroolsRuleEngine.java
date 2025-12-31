package com.mezei.aml.screening.rules;

import com.mezei.aml.common.tx.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DroolsRuleEngine implements RuleEngine {

    private final KieContainer kieContainer;

    @Override
    public List<RuleHit> evaluate(TransactionEvent event) {
        List<RuleHit> hits = new ArrayList<>();

        KieSession session = kieContainer.newKieSession("screeningKSession");
        try {
            session.setGlobal("hits", hits);
            session.insert(new TxFact(event));
            session.fireAllRules();
            return hits;
        } finally {
            session.dispose();
        }
    }
}
