package com.smarterReconcilation.smarterReconcilation.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Setter
@Getter
public class FinancialTransaction {
    @CsvBindByName(column = "transactionId")
    private String transactionId;

    @CsvBindByName(column = "accountNumber")
    private String accountNumber;

    @CsvCustomBindByName(column = "transactionDate", converter = LocalDateConverter.class)
    private LocalDate transactionDate;

    @CsvBindByName(column = "amount")
    private BigDecimal amount;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "reference")
    private String reference;

    @CsvBindByName(column = "transactionType")
    private String transactionType;

    @CsvBindByName(column = "currency")
    private String currency;

    private String sourceSystem;
}