package com.mezei.aml.ingest.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqSmokeTestRunner implements CommandLineRunner {

    private final JmsTemplate jmsTemplate;

    @Override
    public void run(String... args) {
        String destination = "DEV.TEST";
        String message = "Hello from aml-ingest";

        log.info("Sending test message to Artemis queue '{}'...", destination);
        jmsTemplate.convertAndSend(destination, message);
        log.info("Test message sent.");
    }
}

