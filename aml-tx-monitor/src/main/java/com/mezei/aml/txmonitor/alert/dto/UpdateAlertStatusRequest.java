package com.mezei.aml.txmonitor.alert.dto;

import jakarta.validation.constraints.*;

public record UpdateAlertStatusRequest(
        @NotBlank
        @Pattern(regexp = "OPEN|INVESTIGATING|ESCALATED|DISMISSED|CLOSED")
        String status,

        @Size(max = 128)
        String assignedTo
) {}
