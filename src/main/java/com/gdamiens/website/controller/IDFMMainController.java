package com.gdamiens.website.controller;

import com.gdamiens.website.controller.object.gtfs.*;
import com.gdamiens.website.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
public class IDFMMainController {

    private static final Logger log = LoggerFactory.getLogger(IDFMMainController.class);

    private final IDFMMainService idfmMainService;

    private final IDFMAgencyService idfmAgencyService;

    private final IDFMRouteService idfmRouteService;
    public IDFMMainController(IDFMMainService idfmMainService, IDFMAgencyService idfmAgencyService, IDFMRouteService idfmRouteService) {
        this.idfmMainService = idfmMainService;
        this.idfmAgencyService = idfmAgencyService;
        this.idfmRouteService = idfmRouteService;
    }

    @PostMapping("/gtfs")
    @Operation(summary = "Update all data with GTFS", security = @SecurityRequirement(name = "Auth. Token"))
    public ResponseEntity<Void> updateGTFS() {
        try {
            String gtfslink = this.idfmMainService.getGTFSlink();

            if (gtfslink == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            ZipReader zipReader = new ZipReader();
            ZipInputStream zipInputStream = zipReader.readFromUrl(gtfslink);

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                log.info(fileName);

                switch (fileName) {
                    case "agency.txt":
                        CSVReader<AgencyCSV> csvReaderAgency = new CSVReader<>(AgencyCSV.class);
                        List<AgencyCSV> agencyCSVList = csvReaderAgency.readFromZipInputStream(zipInputStream);
                        this.idfmAgencyService.saveAllAgenciesFromAgenciesCSVList(agencyCSVList);
                        break;
                    case "calendar.txt":
                        CSVReader<CalendarCSV> csvReaderCalendar = new CSVReader<>(CalendarCSV.class);
                        List<CalendarCSV> calendarCSVList = csvReaderCalendar.readFromZipInputStream(zipInputStream);
                        break;
                    case "calendar_dates.txt":
                        CSVReader<CalendarDatesCSV> csvReaderCalendarDates = new CSVReader<>(CalendarDatesCSV.class);
                        List<CalendarDatesCSV> calendarDatesCSVList = csvReaderCalendarDates.readFromZipInputStream(zipInputStream);
                        break;
                    case "pathways.txt":
                        CSVReader<PathwaysCSV> csvReaderPathWays = new CSVReader<>(PathwaysCSV.class);
                        List<PathwaysCSV> pathwaysCSVList = csvReaderPathWays.readFromZipInputStream(zipInputStream);
                        break;
                    case "routes.txt":
                        CSVReader<RoutesCSV> csvReaderRoutes = new CSVReader<>(RoutesCSV.class);
                        List<RoutesCSV> routesCSVList = csvReaderRoutes.readFromZipInputStream(zipInputStream);
                        this.idfmRouteService.saveAllRoutesFromRoutesCSVList(routesCSVList);
                        break;
                    case "stop_extensions.txt":
                        CSVReader<StopExtensionsCSV> csvReaderStopExtensions = new CSVReader<>(StopExtensionsCSV.class);
                        List<StopExtensionsCSV> stopExtensionsCSVList = csvReaderStopExtensions.readFromZipInputStream(zipInputStream);
                        break;
                    case "stops.txt":
                        CSVReader<StopsCSV> csvReaderStops = new CSVReader<>(StopsCSV.class);
                        List<StopsCSV> stopsCSVList = csvReaderStops.readFromZipInputStream(zipInputStream);
                        break;
                    case "stop_times.txt":
                        CSVReader<StopTimesCSV> csvReaderStopTimes = new CSVReader<>(StopTimesCSV.class);
                        List<StopTimesCSV> stopTimesCSVList = csvReaderStopTimes.readFromZipInputStream(zipInputStream);
                        break;
                    case "transfers.txt":
                        CSVReader<TransfersCSV> csvReaderTransfersCSV = new CSVReader<>(TransfersCSV.class);
                        List<TransfersCSV> transfersCSVList = csvReaderTransfersCSV.readFromZipInputStream(zipInputStream);
                        break;
                    case "trips.txt":
                        CSVReader<TripsCSV> csvReaderTrips = new CSVReader<>(TripsCSV.class);
                        List<TripsCSV> tripsCSVList = csvReaderTrips.readFromZipInputStream(zipInputStream);
                        break;
                    default:
                        break;
                }

                zipEntry = zipInputStream.getNextEntry();
            }

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            log.info("error during GTFS update");
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
