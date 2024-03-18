package com.gdamiens.website.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Component
public class ApplicationProperties {
    private String idfmKey;

    private String idfmStaticKey;

    public String getIdfmKey() {
        return idfmKey;
    }

    public void setIdfmKey(String idfmKey) {
        this.idfmKey = idfmKey;
    }

    public String getIdfmStaticKey() {
        return idfmStaticKey;
    }

    public void setIdfmStaticKey(String idfmStaticKey) {
        this.idfmStaticKey = idfmStaticKey;
    }
}

