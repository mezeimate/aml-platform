package com.mezei.aml.txmonitor.alert.controller;

import com.mezei.aml.txmonitor.alert.dto.AlertResponse;
import com.mezei.aml.txmonitor.alert.dto.CreateAlertRequest;
import com.mezei.aml.txmonitor.alert.dto.UpdateAlertStatusRequest;
import com.mezei.aml.txmonitor.alert.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@RequestBody @Valid CreateAlertRequest request) {
        AlertResponse alertResponse = alertService.createAlert(request);
        URI location = URI.create("/api/v1/alerts/" + alertResponse.id());
        return ResponseEntity.created(location).body(alertResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable("id") UUID id) {
        Optional<AlertResponse> alert = alertService.getAlertById(id);
        return alert
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> listAlerts(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset) {
        List<AlertResponse> alerts = alertService.listAlerts(limit, offset);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponse> updateAlertDetails(@PathVariable("id") UUID id,
            @RequestBody @Valid CreateAlertRequest request) {
        boolean updated = alertService.updateAlertDetails(id, request);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        Optional<AlertResponse> refreshed = alertService.getAlertById(id);
        return refreshed
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateAlertStatus(@PathVariable("id") UUID id,
            @RequestBody @Valid UpdateAlertStatusRequest request) {
        boolean updated = alertService.updateAlertStatus(id, request.status(), request.assignedTo());
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable("id") UUID id) {
        boolean deleted = alertService.deleteAlert(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
