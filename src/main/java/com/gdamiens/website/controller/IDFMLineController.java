package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.LineDTO;
import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.service.IDFMLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IDFMLineController {

    private static final Logger log = LoggerFactory.getLogger(IDFMLineController.class);

    private final IDFMLineService idfmLineService;

    public IDFMLineController(IDFMLineService idfmLineService) {
        this.idfmLineService = idfmLineService;
    }

    @GetMapping("/lines")
    @Operation(summary = "Get list of lines by their transport mode", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<LinesDTO> getLines() {
        try {
            Map<TransportMode, List<LineDTO>> linesByTransportMode = this.idfmLineService.getLinesByTransportMode();

            EnumMap<TransportMode, Integer> count = new EnumMap<>(TransportMode.class);

            linesByTransportMode
                .forEach((transportMode, idfmLines) -> count.put(transportMode, idfmLines.size()));

            return new ResponseEntity<>(new LinesDTO(linesByTransportMode, count), HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get lines request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/update-shapes")
    @Operation(summary = "Update line shapes", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateShapes() {
        try {
            this.idfmLineService.updateLineShapes();

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during IDFM get lines request");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
