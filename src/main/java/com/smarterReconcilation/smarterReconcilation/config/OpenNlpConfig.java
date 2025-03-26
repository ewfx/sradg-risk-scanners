package com.smarterReconcilation.smarterReconcilation.config;

import opennlp.tools.namefind.TokenNameFinderModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class OpenNlpConfig {

    @Bean
    public TokenNameFinderModel amountModel() throws IOException {
        try (InputStream modelIn = new ClassPathResource("en-ner-amount.bin").getInputStream()) {
            return new TokenNameFinderModel(modelIn);
        }
    }
}