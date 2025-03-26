package com.smarterReconcilation.smarterReconcilation.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OpenNlpService {

    private final NameFinderME amountFinder;
    private final SimpleTokenizer tokenizer;
    private final Pattern amountPattern;

    public OpenNlpService(TokenNameFinderModel amountModel) {
        this.amountFinder = new NameFinderME(amountModel);
        this.tokenizer = SimpleTokenizer.INSTANCE;
        this.amountPattern = Pattern.compile("\\$?\\d+(,\\d{3})*(\\.\\d{1,2})?");
    }

    public List<String> detectAmounts(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        String[] tokens = tokenizer.tokenize(text);
        Span[] spans = amountFinder.find(tokens);  // Now using NameFinderME

        List<String> amounts = new ArrayList<>();
        for (Span span : spans) {
            String[] slice = Arrays.copyOfRange(tokens, span.getStart(), span.getEnd());
            amounts.add(String.join(" ", slice));
        }

        // Fallback to regex if no amounts detected
        if (amounts.isEmpty()) {
            var matcher = amountPattern.matcher(text);
            while (matcher.find()) {
                amounts.add(matcher.group());
            }
        }

        // Clear adaptive data to keep the model stateless
        amountFinder.clearAdaptiveData();

        return amounts;
    }

    public boolean isSuspiciousDescription(String description, BigDecimal transactionAmount) {
        if (description == null || transactionAmount == null) return false;

        List<String> detectedAmounts = detectAmounts(description);
        if (detectedAmounts.isEmpty()) return false;

        // Check if any mentioned amount differs significantly from transaction amount
        for (String amountStr : detectedAmounts) {
            try {
                BigDecimal mentionedAmount = parseAmount(amountStr);
                if (isSignificantDifference(transactionAmount, mentionedAmount)) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // Skip malformed amounts
            }
        }

        return false;
    }

    private BigDecimal parseAmount(String amountStr) {
        return new BigDecimal(amountStr.replaceAll("[^\\d.]", ""));
    }

    private boolean isSignificantDifference(BigDecimal actual, BigDecimal mentioned) {
        BigDecimal difference = actual.subtract(mentioned).abs();
        BigDecimal percentageDiff = difference.divide(actual.max(mentioned), 4, RoundingMode.HALF_UP);
        return percentageDiff.compareTo(new BigDecimal("0.1")) > 0; // More than 10% difference
    }

    // ... rest of the class remains the same ...
}