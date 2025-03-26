package com.smarterReconcilation.smarterReconcilation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ReconciliationRequest {
    @NotNull(message = "Source file is required")
    private MultipartFile sourceFile;

    @NotNull(message = "Target file is required")
    private MultipartFile targetFile;

    private MultipartFile referenceFile;
    private String matchingStrategy = "COMPOSITE_KEY";
    private Double amountTolerance = 0.1;
    private boolean detectAnomalies = true;
}