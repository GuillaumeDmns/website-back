package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public abstract class AbstractIDFMService {

    private final ApplicationProperties applicationProperties;

    private final String idfmStaticKey;

    protected AbstractIDFMService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.idfmStaticKey = this.applicationProperties.getIdfmStaticKey();
    }

    public String getIdfmStaticKey() {
        return idfmStaticKey;
    }

    protected HttpEntity<String> prepareHttpRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Accept-encoding", "gzip, deflate");
        headers.set("apiKey", this.applicationProperties.getIdfmKey());

        return new HttpEntity<>(headers);
    }
}
