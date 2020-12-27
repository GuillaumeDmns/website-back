package com.gdamiens.website.controller;

import com.gdamiens.website.WsConsumer;
import com.gdamiens.website.ratp.wsdl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RATPController {
    @Autowired
    WsConsumer wsConsumer;

    @GetMapping("/rerA")
    public ResponseEntity<String> getRerAInfos() {
        try {
            GetLinesResponse response = wsConsumer.getLines("RA");
            if (response.getReturn().size() > 0) {
                List<Line> lineList = response.getReturn();
                Line line = lineList.get(0);
                String returnString = "Line [code="
                        + line.getCode()
                        + ", codeStif="
                        + line.getCodeStif()
                        + ", id="
                        + line.getId()
                        + ", image="
                        + line.getImage()
                        + ", name="
                        + line.getName()
                        + ", realm="
                        + line.getRealm()
                        + ", reseau="
                        + line.getReseau() + "]";

                return new ResponseEntity<>(returnString, HttpStatus.OK);
            }
            return new ResponseEntity<>("rien :(", HttpStatus.NOT_FOUND);

        }
        catch (Exception e) {
            return new ResponseEntity<>("Error : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/next/{lineId}/{stationName}")
    public ResponseEntity<List<Mission>> getNext(@PathVariable String lineId, @PathVariable String stationName) {
        try {
            GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, stationName);

            WrMissions wrMissions = response.getReturn();
            List<Mission> missionList = wrMissions.getMissions();
            return new ResponseEntity<>(missionList, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}