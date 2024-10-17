package com.igs.vault.secret;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author alive
 */
@ConfigurationProperties
@EnableConfigurationProperties(CertificatesConfiguration.class)
@Data
public class CertificatesConfiguration {

    private String cert;

    private String key;

}

