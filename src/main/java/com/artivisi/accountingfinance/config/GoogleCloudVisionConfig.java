package com.artivisi.accountingfinance.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "google.cloud.vision")
@Getter
@Setter
public class GoogleCloudVisionConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudVisionConfig.class);

    private boolean enabled;
    private String credentialsPath;

    @Bean
    @ConditionalOnProperty(name = "google.cloud.vision.enabled", havingValue = "true")
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("Google Cloud Vision credentials path not configured, using default credentials");
            return ImageAnnotatorClient.create();
        }

        log.info("Initializing Google Cloud Vision with credentials from: {}", credentialsPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return ImageAnnotatorClient.create(settings);
    }
}
