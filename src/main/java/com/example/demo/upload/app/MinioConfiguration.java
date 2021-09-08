package com.example.demo.upload.app;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(MinioProperties.class)
@Slf4j
@Configuration
@ConditionalOnProperty(name = "minio.enable", havingValue = "true")
public class MinioConfiguration {

    private MinioProperties minioProperties;

    public MinioConfiguration(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = null;
        try {
            minioClient = new MinioClient(minioProperties.getEndpoint(), minioProperties.getPort(), minioProperties.getAccessKey(), minioProperties.getSecretKey());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return minioClient;
    }
}
