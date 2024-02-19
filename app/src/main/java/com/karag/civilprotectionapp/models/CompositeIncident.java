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

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(double dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public int getNumOfReports() {
        return numOfReports;
    }

    public void setNumOfReports(int numOfReports) {
        this.numOfReports = numOfReports;
    }

    public List<MyIncident> getRelatedReports() {
        return relatedReports;
    }

    public void setRelatedReports(List<MyIncident> relatedReports) {
        this.relatedReports = relatedReports;
    }
}
