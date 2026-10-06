package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
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

    private ApiQuota apiQuota;

    @Autowired
    void setApiQuota(ApiQuota apiQuota) {
        this.apiQuota = apiQuota;
    }

    /** Counts a request against the daily quota of [api] (refused when used up) */
    protected void consume(ApiQuota.Api api) {
        if (apiQuota != null) {
            apiQuota.consume(api);
        }
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
