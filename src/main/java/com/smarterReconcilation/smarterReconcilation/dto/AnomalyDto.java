package com.smarterReconcilation.smarterReconcilation.dto;

import lombok.Data;

@Data
public class AnomalyDto {
    private String transactionId;
    private String anomalyType;
    private String description;
    private String severity;
    private String suggestedAction;
}
