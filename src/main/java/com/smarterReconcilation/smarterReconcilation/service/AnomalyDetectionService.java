package com.smarterReconcilation.smarterReconcilation.service;

import com.smarterReconcilation.smarterReconcilation.dto.AnomalyDto;
import com.smarterReconcilation.smarterReconcilation.model.FinancialTransaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectionService {

    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("1000000"); // 1 million
    private static final int OLD_TRANSACTION_DAYS = 365;

    @Autowired
    OpenNlpService openNlpService;

    public AnomalyDetectionService(OpenNlpService openNlpService) {
        this.openNlpService = openNlpService;
    }

    public List<AnomalyDto> detectAnomalies(List<FinancialTransaction> transactions) {
        List<AnomalyDto> anomalies = new ArrayList<>();

        for (FinancialTransaction txn : transactions) {
            checkForLargeAmount(txn, anomalies);
            checkForAmountDescriptionMismatch(txn, anomalies);
            checkForFutureDatedTransactions(txn, anomalies);
            checkForOldTransactions(txn, anomalies);
            checkForRoundAmounts(txn, anomalies);
            checkForSuspiciousDescriptions(txn, anomalies);
        }

        return anomalies;
    }

    private void checkForLargeAmount(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (txn.getAmount() != null && txn.getAmount().compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            anomalies.add(createAnomaly(txn, "LARGE_AMOUNT",
                    String.format("Transaction amount %s is unusually large", txn.getAmount()), "HIGH"));
        }
    }

    private void checkForAmountDescriptionMismatch(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (openNlpService.isSuspiciousDescription(txn.getDescription(), txn.getAmount())) {
            anomalies.add(createAnomaly(txn, "AMOUNT_DESCRIPTION_MISMATCH",
                    "Description mentions amounts that don't match transaction amount", "MEDIUM"));
        }
    }

    private void checkForFutureDatedTransactions(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (txn.getTransactionDate() != null && txn.getTransactionDate().isAfter(LocalDate.now())) {
            anomalies.add(createAnomaly(txn, "FUTURE_DATED",
                    "Transaction is dated in the future", "HIGH"));
        }
    }

    private void checkForOldTransactions(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (txn.getTransactionDate() != null &&
                ChronoUnit.DAYS.between(txn.getTransactionDate(), LocalDate.now()) > OLD_TRANSACTION_DAYS) {
            anomalies.add(createAnomaly(txn, "OLD_TRANSACTION",
                    "Transaction is more than 1 year old", "MEDIUM"));
        }
    }

    private void checkForRoundAmounts(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (isRoundAmount(txn.getAmount())) {
            anomalies.add(createAnomaly(txn, "ROUND_AMOUNT",
                    "Transaction amount is a round number (possible manual entry)", "LOW"));
        }
    }

    private void checkForSuspiciousDescriptions(FinancialTransaction txn, List<AnomalyDto> anomalies) {
        if (hasSuspiciousKeywords(txn.getDescription())) {
            anomalies.add(createAnomaly(txn, "SUSPICIOUS_DESCRIPTION",
                    "Transaction description contains suspicious keywords", "HIGH"));
        }
    }

    private boolean isRoundAmount(BigDecimal amount) {
        if (amount == null) return false;
        try {
            return amount.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) == 0;
        } catch (ArithmeticException e) {
            return false;
        }
    }

    private boolean hasSuspiciousKeywords(String description) {
        if (StringUtils.isBlank(description)) return false;

        String lowerDesc = description.toLowerCase();
        return lowerDesc.contains("adjustment") ||
                lowerDesc.contains("correction") ||
                lowerDesc.contains("miscellaneous") ||
                lowerDesc.contains("variance") ||
                lowerDesc.contains("difference");
    }

    private AnomalyDto createAnomaly(FinancialTransaction txn, String type, String desc, String severity) {
        AnomalyDto anomaly = new AnomalyDto();
        anomaly.setTransactionId(txn.getTransactionId());
        anomaly.setAnomalyType(type);
        anomaly.setDescription(desc);
        anomaly.setSeverity(severity);
        anomaly.setSuggestedAction(getSuggestedAction(type));
        return anomaly;
    }

    private String getSuggestedAction(String anomalyType) {
        return switch (anomalyType) {
            case "LARGE_AMOUNT", "FUTURE_DATED", "SUSPICIOUS_DESCRIPTION" ->
                    "Immediate review required by senior staff";
            case "AMOUNT_DESCRIPTION_MISMATCH", "OLD_TRANSACTION" ->
                    "Review transaction and verify with original documentation";
            case "ROUND_AMOUNT" ->
                    "Verify if this is a manual adjustment entry";
            default -> "Review transaction manually";
        };
    }
}
