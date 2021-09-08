package com.example.demo.upload.app;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MinioProperties.MINO_PREFIX)
@Data
public class MinioProperties {
    public static final String MINO_PREFIX = "minio";

    private boolean enable;

    private String endpoint;

    private int port;

    private String accessKey;

    private String secretKey;
}
