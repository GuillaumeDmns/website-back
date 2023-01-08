package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.StopAreaCSV;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.repository.IDFMStopAreaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.gdamiens.website.utils.GeoUtils.convertLambert94ToLatLong;

@Service
public class IDFMStopAreaService {

    private final IDFMStopAreaRepository idfmStopAreaRepository;

    public IDFMStopAreaService(IDFMStopAreaRepository idfmStopAreaRepository) {
        this.idfmStopAreaRepository = idfmStopAreaRepository;
    }

    public void saveAllStopAreasFromCSV(List<StopAreaCSV> stopAreas) {
        convertLambert94ToLatLong(stopAreas);

        this.idfmStopAreaRepository.saveAll(
            stopAreas.parallelStream().map(IDFMStopArea::new).collect(Collectors.toList())
        );
    }

    public IDFMStopArea getStop(Integer stopAreaId) {
        return this.idfmStopAreaRepository.findById(stopAreaId).orElse(null);
    }

    public List<IDFMStopArea> getStopAreasFromLineId(String lineId) {
        return this.idfmStopAreaRepository.getStopAreasFromLineId(lineId);
    }
}
