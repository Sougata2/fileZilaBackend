package com.sougata.filezilabackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String tokenUri;
}
