package com.karag.civilprotectionapp.models;

import java.util.Date;
import java.util.List;

public class CompositeIncident {

    private String emergencyType;
    private Date datetime;
    private double latitude;
    private double longitude;
    private double dangerLevel;
    private int numOfReports;
    private List<MyIncident> relatedReports;

    public CompositeIncident(String emergencyType, Date datetime, double latitude, double longitude, double dangerLevel, int numOfReports, List<MyIncident> relatedReports) {
        this.emergencyType = emergencyType;
        this.datetime = datetime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangerLevel = dangerLevel;
        this.numOfReports = numOfReports;
        this.relatedReports = relatedReports;
    }
}
