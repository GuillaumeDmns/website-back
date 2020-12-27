package com.gdamiens.website;

import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;


public class WsConsumer extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(WsConsumer.class);

    public GetLinesResponse getLines(String lineId) {

        ObjectFactory factory = new ObjectFactory();
        Line line = factory.createLine();
        line.setId(lineId);

        GetLines getLines = factory.createGetLines();
        getLines.setLine(line);

        log.info("Requesting lineId " + lineId);

        return (GetLinesResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getLines);
    }

    public GetMissionsNextResponse getMissionsNext(String lineId, String stationName) {

        ObjectFactory factory = new ObjectFactory();

        GetMissionsNext getMissionsNext = factory.createGetMissionsNext();
        Direction direction = factory.createDirection();
        Station station = factory.createStation();
        Line line = factory.createLine();

        line.setId(lineId);
        station.setLine(line);
        station.setName(stationName);
        direction.setSens("*");

        getMissionsNext.setDirection(direction);
        getMissionsNext.setStation(station);

        return (GetMissionsNextResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getMissionsNext);
    }

}