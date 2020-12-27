package com.gdamiens.website.configuration;

import com.gdamiens.website.WsConsumer;
import com.gdamiens.website.utils.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class WsConfiguration {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.gdamiens.website.ratp.wsdl");
        return marshaller;
    }

    @Bean
    public WsConsumer wsClient(Jaxb2Marshaller marshaller) {
        WsConsumer client = new WsConsumer();
        client.setDefaultUri(Constants.RATP_SOAP_URL);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}