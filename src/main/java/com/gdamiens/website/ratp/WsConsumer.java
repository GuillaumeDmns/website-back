package com.gdamiens.website.ratp;

import com.gdamiens.website.controller.object.LineRequest;
import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;


public class WsConsumer extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(WsConsumer.class);

    public GetStationsResponse getStations(String id, String name, String sens, LineRequest lineRequest, Integer limit, Boolean isSortedAlpha) {

        ObjectFactory factory = new ObjectFactory();

        if (id == null && name == null && sens == null && lineRequest == null) {
            return null;
        }


        Station station = factory.createStation();
        station.setId(id);
        station.setName(name);

        Direction direction = factory.createDirection();
        direction.setSens(sens);
        station.setDirection(direction);

        if (lineRequest != null) {
            Line line = factory.createLine();
            if (lineRequest.getRealm() != null && lineRequest.getRealm().equals("r")) {
                line.setRealm(lineRequest.getRealm());
            }
            station.setLine(line);
        }

        GetStations getStations = factory.createGetStations();
        getStations.setStation(station);
        getStations.setLimit(limit);
        getStations.setSortAlpha(isSortedAlpha);

        log.info("Requesting stationId " + id + ", name " + name + ", sens " + sens + ", line " + lineRequest + ", limit " + limit + ", isSortedAlpha " + isSortedAlpha);

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

    public GetMissionsNextResponse getMissionsNext(String lineId, String stationId, Integer limit) {

        ObjectFactory factory = new ObjectFactory();

        GetMissionsNext getMissionsNext = factory.createGetMissionsNext();
        Direction direction = factory.createDirection();
        Station station = factory.createStation();
        Line line = factory.createLine();

        line.setId(lineId);
        station.setLine(line);
        station.setId(stationId);
        direction.setSens("*");

        getMissionsNext.setDirection(direction);
        getMissionsNext.setStation(station);
        getMissionsNext.setLimit(limit);

        log.info("Requesting next missions for stationId " + stationId + " and lineId " + lineId);

        return (GetMissionsNextResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getMissionsNext);
    }

    public GetStationsResponse getStations(String lineId, String stationName) {

        ObjectFactory factory = new ObjectFactory();

        GetStations getStations = factory.createGetStations();
        Station station = factory.createStation();
        Line line = factory.createLine();

        line.setId(lineId);
        station.setName(stationName);
        station.setLine(line);

        getStations.setStation(station);

        log.info("Requesting station for stationName " + stationName + " and lineId " + lineId);

        return (GetStationsResponse) getWebServiceTemplate()
                .marshalSendAndReceive(Constants.RATP_SOAP_URL, getStations);
    }

}