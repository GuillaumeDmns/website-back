package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.StopAreaCSV;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.repository.IDFMStopAreaRepository;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.gdamiens.website.utils.GeoUtils.convertLambert94ToLatLong;

@Service
public class IDFMStopAreaService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopAreaService.class);

    private final IDFMStopAreaRepository idfmStopAreaRepository;

    public IDFMStopAreaService(IDFMStopAreaRepository idfmStopAreaRepository) {
        this.idfmStopAreaRepository = idfmStopAreaRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all stop areas");
        this.idfmStopAreaRepository.deleteAllInBatch();
        log.info("Finish deleting all stops areas");
    }

    public void saveAllStopAreasFromCSV() {
        CSVReader<StopAreaCSV> csvReader = new CSVReader<>(StopAreaCSV.class);

        List<StopAreaCSV> stopAreas = csvReader.readFromUrl(Constants.IDFM_STOP_AREAS_URL);

        log.info("Start importing stop areas");
        log.info("{} stop areas to process", stopAreas.size());

        convertLambert94ToLatLong(stopAreas);

        this.idfmStopAreaRepository.saveAll(
            stopAreas.parallelStream().map(IDFMStopArea::new).collect(Collectors.toList())
        );

        log.info("Finish importing stop areas");
    }

    public IDFMStopArea getStop(Integer stopAreaId) {
        return this.idfmStopAreaRepository.findById(stopAreaId).orElse(null);
    }

    public List<IDFMStopArea> getStopAreasFromLineId(String lineId) {
        return this.idfmStopAreaRepository.getStopAreasFromLineId(lineId);
    }

    public void saveStops(List<IDFMStopArea> stops) {
        this.idfmStopAreaRepository.saveAll(stops);
    }
}
