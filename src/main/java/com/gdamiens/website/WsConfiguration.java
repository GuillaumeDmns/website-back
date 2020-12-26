package com.gdamiens.website;

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
        client.setDefaultUri("http://opendata-tr.ratp.fr/wsiv/services/Wsiv");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}