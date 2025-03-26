package com.smarterReconcilation.smarterReconcilation.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.smarterReconcilation.smarterReconcilation.exception.FileProcessingException;
import com.smarterReconcilation.smarterReconcilation.model.FinancialTransaction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvProcessingService {

    public List<FinancialTransaction> parseCsvFile(MultipartFile file, String sourceSystem) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<FinancialTransaction> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(FinancialTransaction.class);

            CsvToBean<FinancialTransaction> csvToBean = new CsvToBeanBuilder<FinancialTransaction>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<FinancialTransaction> transactions = csvToBean.parse();

            // Set source system for all transactions
            transactions.forEach(t -> t.setSourceSystem(sourceSystem));

            return transactions;
        }
    }

    /*private void normalizeTransactionData(FinancialTransaction txn) {
        // Normalize amount
        if (txn.getAmount() == null && StringUtils.isNotBlank(txn.getAmountStr())) {
            try {
                txn.setAmount(new BigDecimal(txn.getAmountStr().replaceAll("[^\\d.]", "")));
            } catch (NumberFormatException e) {
                // Leave amount as null if parsing fails
            }
        }

        // Normalize date
        if (txn.getTransactionDate() == null && StringUtils.isNotBlank(txn.getDateStr())) {
            try {
                txn.setTransactionDate(LocalDate.parse(txn.getDateStr(), DateTimeFormatter.ISO_DATE));
            } catch (DateTimeParseException e1) {
                try {
                    txn.setTransactionDate(LocalDate.parse(txn.getDateStr(),
                            DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                } catch (DateTimeParseException e2) {
                    // Leave date as null if parsing fails
                }
            }
        }

        // Generate transaction ID if missing
        if (StringUtils.isBlank(txn.getTransactionId())) {
            txn.setTransactionId(generateTransactionId(txn));
        }
    }*/

    private String generateTransactionId(FinancialTransaction txn) {
        return String.format("%s-%s-%s-%d",
                txn.getSourceSystem(),
                txn.getAccountNumber(),
                txn.getTransactionDate() != null ? txn.getTransactionDate().toString() : "NODATE",
                System.currentTimeMillis() % 10000);
    }
}