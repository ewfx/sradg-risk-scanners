package com.smarterReconcilation.smarterReconcilation.service;

import com.smarterReconcilation.smarterReconcilation.dto.ReconciliationResult;
import com.smarterReconcilation.smarterReconcilation.model.FinancialTransaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ReconciliationService {

    public ReconciliationResult reconcile(List<FinancialTransaction> sourceTransactions,
                                          List<FinancialTransaction> targetTransactions,
                                          double amountTolerance) {
        ReconciliationResult result = new ReconciliationResult();

        // Group transactions by composite key
        Map<String, List<FinancialTransaction>> sourceMap = groupTransactions(sourceTransactions);
        Map<String, List<FinancialTransaction>> targetMap = groupTransactions(targetTransactions);

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(sourceMap.keySet());
        allKeys.addAll(targetMap.keySet());

        List<FinancialTransaction> discrepancies = new ArrayList<>();
        int matched = 0;
        int sourceOnly = 0;
        int targetOnly = 0;

        for (String key : allKeys) {
            List<FinancialTransaction> sourceGroup = sourceMap.getOrDefault(key, Collections.emptyList());
            List<FinancialTransaction> targetGroup = targetMap.getOrDefault(key, Collections.emptyList());

            if (sourceGroup.isEmpty()) {
                targetOnly += targetGroup.size();
                discrepancies.addAll(targetGroup);
                continue;
            }

            if (targetGroup.isEmpty()) {
                sourceOnly += sourceGroup.size();
                discrepancies.addAll(sourceGroup);
                continue;
            }

            // Match transactions within the group
            matchTransactionGroups(sourceGroup, targetGroup, discrepancies, amountTolerance);
            matched += Math.min(sourceGroup.size(), targetGroup.size());
        }

        result.setMatchedRecords(matched);
        result.setSourceOnlyRecords(sourceOnly);
        result.setTargetOnlyRecords(targetOnly);
        result.setUnmatchedRecords(discrepancies.size());
        result.setDiscrepancies(discrepancies);
        result.calculateSummary();

        return result;
    }

    private Map<String, List<FinancialTransaction>> groupTransactions(List<FinancialTransaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(this::getTransactionKey));
    }

    private void matchTransactionGroups(List<FinancialTransaction> sourceGroup,
                                        List<FinancialTransaction> targetGroup,
                                        List<FinancialTransaction> discrepancies,
                                        double tolerance) {
        // Simple one-to-one matching - can be enhanced for batch matching
        FinancialTransaction sourceTxn = sourceGroup.get(0);
        FinancialTransaction targetTxn = targetGroup.get(0);

        if (!isTransactionMatch(sourceTxn, targetTxn, tolerance)) {
            discrepancies.add(sourceTxn);
            discrepancies.add(targetTxn);
        }
    }

    private boolean isTransactionMatch(FinancialTransaction source, FinancialTransaction target, double tolerance) {
        return isAmountWithinTolerance(source.getAmount(), target.getAmount(), tolerance) &&
                StringUtils.equalsIgnoreCase(source.getCurrency(), target.getCurrency()) &&
                StringUtils.equalsIgnoreCase(source.getTransactionType(), target.getTransactionType());
    }

    private String getTransactionKey(FinancialTransaction txn) {
        return String.format("%s|%s|%s|%s",
                txn.getAccountNumber(),
                txn.getReference(),
                txn.getTransactionDate(),
                txn.getTransactionType());
    }

    private boolean isAmountWithinTolerance(BigDecimal amount1, BigDecimal amount2, double tolerance) {
        if (amount1 == null || amount2 == null) return false;

        BigDecimal difference = amount1.subtract(amount2).abs();
        BigDecimal maxDifference = amount1.abs()
                .max(amount2.abs())
                .multiply(BigDecimal.valueOf(tolerance / 100.0))
                .setScale(2, RoundingMode.HALF_UP);

        return difference.compareTo(maxDifference) <= 0;
    }
}
