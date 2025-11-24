package com.mezei.aml.ingest.mq;

public record DlqError(
        String errorType,
        String errorMessage,
        String rawJson
) {}
