package com.gdamiens.website.model.mapper;

import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.TransportMode;

public class LineMapper {

    private LineMapper() {}

    public static IDFMLine csvToDb(LineCSV lineCSV) {
        IDFMLine idfmLine = new IDFMLine();

        idfmLine.setId(lineCSV.getLineId());
        idfmLine.setName(lineCSV.getName());

        switch (lineCSV.getTransportMode()) {
            case "bus":
                idfmLine.setTransportMode("Noctilien".equals(lineCSV.getNetworkName()) ? TransportMode.NOCTILIEN : TransportMode.BUS);
                break;
            case "rail":
                if ("TER".equals(lineCSV.getNetworkName())) idfmLine.setTransportMode(TransportMode.TER);
                else if ("Transilien".equals(lineCSV.getNetworkName())) idfmLine.setTransportMode(TransportMode.TRANSILIEN);
                else if ("RER".equals(lineCSV.getNetworkName())) idfmLine.setTransportMode(TransportMode.RER);
                else idfmLine.setTransportMode(TransportMode.SHUTTLE);
                break;
            case "metro":
                idfmLine.setTransportMode(TransportMode.METRO);
                break;
            case "tram":
                idfmLine.setTransportMode(TransportMode.TRAM);
                break;
            case "funicular":
                idfmLine.setTransportMode(TransportMode.FUNICULAR);
                break;
            default:
                idfmLine.setTransportMode(TransportMode.BUS);
        }

        idfmLine.setOperatorId(lineCSV.getOperatorId());
        idfmLine.setLineIdColor(lineCSV.getLineIdColor());
        idfmLine.setLineIdBackgroundColor(lineCSV.getLineIdBackgroundColor());


        return idfmLine;
    }
}
