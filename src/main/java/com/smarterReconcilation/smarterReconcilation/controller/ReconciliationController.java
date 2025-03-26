package com.smarterReconcilation.smarterReconcilation.controller;

import com.smarterReconcilation.smarterReconcilation.dto.ReconciliationRequest;
import com.smarterReconcilation.smarterReconcilation.dto.ReconciliationResult;
import com.smarterReconcilation.smarterReconcilation.exception.FileProcessingException;
import com.smarterReconcilation.smarterReconcilation.model.FinancialTransaction;
import com.smarterReconcilation.smarterReconcilation.service.AnomalyDetectionService;
import com.smarterReconcilation.smarterReconcilation.service.CsvProcessingService;
import com.smarterReconcilation.smarterReconcilation.service.ReconciliationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reconcile")
public class ReconciliationController {

    private final CsvProcessingService csvProcessingService;
    private final ReconciliationService reconciliationService;
    private final AnomalyDetectionService anomalyDetectionService;

    public ReconciliationController(CsvProcessingService csvProcessingService,
                                    ReconciliationService reconciliationService,
                                    AnomalyDetectionService anomalyDetectionService) {
        this.csvProcessingService = csvProcessingService;
        this.reconciliationService = reconciliationService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReconciliationResult> reconcileFiles(
            @RequestPart("sourceFile") MultipartFile sourceFile,
            @RequestPart("targetFile") MultipartFile targetFile,
            @RequestPart(value = "referenceFile", required = false) MultipartFile referenceFile,
            @RequestParam(value = "amountTolerance", defaultValue = "0.1") double amountTolerance,
            @RequestParam(value = "detectAnomalies", defaultValue = "true") boolean detectAnomalies) {

        try {
            // Process CSV files
            List<FinancialTransaction> sourceTxns = csvProcessingService.parseCsvFile(sourceFile, "SOURCE");
            List<FinancialTransaction> targetTxns = csvProcessingService.parseCsvFile(targetFile, "TARGET");

            // Perform reconciliation
            ReconciliationResult result = reconciliationService.reconcile(
                    sourceTxns, targetTxns, amountTolerance);

            // Detect anomalies if requested
            if (detectAnomalies) {
                List<FinancialTransaction> allTxns = new ArrayList<>();
                allTxns.addAll(sourceTxns);
                allTxns.addAll(targetTxns);
                result.setAnomalies(anomalyDetectionService.detectAnomalies(allTxns));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process reconciliation", e);
        }
    }
}