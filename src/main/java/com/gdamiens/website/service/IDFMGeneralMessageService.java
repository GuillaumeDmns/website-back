package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.InfoMessageCustom;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.idfm.IDFMResponse;
import com.gdamiens.website.idfm.InfoMessage;
import com.gdamiens.website.idfm.Siri;
import com.gdamiens.website.idfm.ServiceDelivery;
import com.gdamiens.website.idfm.GeneralMessageDelivery;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IDFMGeneralMessageService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMGeneralMessageService.class);

    private final HttpComponentsClientHttpRequestFactory requestFactory;


    public IDFMGeneralMessageService(ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom().build());
    }

    public List<InfoMessageCustom> getGeneralMessages(String lineId, Integer stopId, String url) {
        HttpEntity<String> request = this.prepareHttpRequest();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        if (lineId != null) {
            uriComponentsBuilder.queryParam("LineRef", "STIF:Line::" + lineId + ":");
        }

        if (stopId != null) {
            uriComponentsBuilder.queryParam("StopPointRef", "STIF:StopPoint:Q:" + stopId + ":");
        }

        ResponseEntity<IDFMResponse> response = new RestTemplate(this.requestFactory).exchange(uriComponentsBuilder.build().toUri(), HttpMethod.GET, request, IDFMResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomException("IDFM response getGeneralMessage != 200", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<IDFMResponse> optionalIDFMResponse = Optional.ofNullable(response.getBody());

        List<InfoMessage> infoMessages = optionalIDFMResponse
            .map(IDFMResponse::getSiri)
            .map(Siri::getServiceDelivery)
            .map(ServiceDelivery::getGeneralMessageDelivery)
            .map(generalMessageDeliveries -> generalMessageDeliveries.get(0))
            .map(GeneralMessageDelivery::getInfoMessage)
            .orElseThrow(() -> new CustomException("IDFM response body does not contain any general information", HttpStatus.INTERNAL_SERVER_ERROR));

        return infoMessages
            .stream()
            .map(InfoMessageCustom::new)
            .collect(Collectors.toList());


    }
}
