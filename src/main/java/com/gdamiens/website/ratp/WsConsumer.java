package com.gdamiens.website.ratp;

import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;


public class WsConsumer extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(WsConsumer.class);

    public GetStationsResponse getStations(String stationName) {

        ObjectFactory factory = new ObjectFactory();

        GetStations getStations = factory.createGetStations();
        Station station = factory.createStation();

        station.setName(stationName);
        getStations.setStation(station);

        log.info("Requesting stationName " + stationName);

        return (GetStationsResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getStations);
    }

    public GetLinesResponse getLines(String lineId,
                                     String code,
                                     String codeStif,
                                     String realm,
                                     String codeReseau) {

        ObjectFactory factory = new ObjectFactory();

        if (lineId == null && code == null && codeStif == null && codeReseau == null) {
            return null;
        }

        Line line = factory.createLine();
        line.setId(lineId);
        line.setCode(code);
        line.setCodeStif(codeStif);
        if (realm != null && realm.equals("r")) {
            line.setRealm(realm);
        }
        if (codeReseau != null) {
            Reseau reseau = factory.createReseau();
            reseau.setCode(codeReseau);
            line.setReseau(reseau);
        }

        GetLines getLines = factory.createGetLines();
        getLines.setLine(line);

        log.info("Requesting lineId " + lineId + ", code " + code + ", codeStif " + codeStif + ", realm " + realm + ", reseau " + codeReseau);

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

        log.info("Requesting next missions for stationName " + stationName + " and lineId " + lineId);

        return (GetMissionsNextResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getMissionsNext);
    }

}