package com.gdamiens.website.utils;

import com.gdamiens.website.controller.object.StopCSV;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.util.List;

public class GeoUtils {

    private GeoUtils() {}

    public static <T extends StopCSV> void convertLambert94ToLatLong(List<T> stops) {

        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem lambert93 = crsFactory.createFromName("epsg:2154");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CoordinateTransform wgsToUtm = ctFactory.createTransform(lambert93, wgs84);

        stops.parallelStream().forEach(stop -> {
            ProjCoordinate result = new ProjCoordinate();
            wgsToUtm.transform(new ProjCoordinate(stop.getLongitude(), stop.getLatitude()), result);
            stop.setLongitude(result.x);
            stop.setLatitude(result.y);
            }
        );
    }
}
