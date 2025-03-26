package com.smarterReconcilation.smarterReconcilation.dto;

import com.smarterReconcilation.smarterReconcilation.model.FinancialTransaction;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReconciliationResult {
    private int matchedRecords;
    private int unmatchedRecords;
    private int sourceOnlyRecords;
    private int targetOnlyRecords;
    private List<FinancialTransaction> discrepancies;
    private Map<String, String> summary;
    private List<AnomalyDto> anomalies;

    public void calculateSummary() {
        int totalRecords = matchedRecords + unmatchedRecords;
        double matchPercentage = totalRecords > 0 ?
                (matchedRecords * 100.0) / totalRecords : 0.0;

        this.summary = Map.of(
                "matchPercentage", String.format("%.2f%%", matchPercentage),
                "totalRecords", String.valueOf(totalRecords)
        );
    }
}