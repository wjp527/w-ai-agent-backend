package com.wjp.waiagentbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "email")
public class MailConfig {

    private String subject;

    private String contentPrefix;

    private String contentSuffix;

}
