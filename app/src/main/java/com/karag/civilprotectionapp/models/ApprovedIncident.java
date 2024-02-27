package com.karag.civilprotectionapp.models;

import java.util.Date;
import java.util.List;

public class ApprovedIncident {
    private String description;
    private String emergencyType;
    private List<String> imageFilenames;
    private Date datetime;
    private double longitude;
    private double latitude;

    public ApprovedIncident(String description, String emergencyType, List<String> imageFilenames, Date datetime, double longitude, double latitude) {
        this.description = description;
        this.emergencyType = emergencyType;
        this.imageFilenames = imageFilenames;
        this.datetime = datetime;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public List<String> getImageFilenames() {
        return imageFilenames;
    }

    public void setImageFilenames(List<String> imageFilenames) {
        this.imageFilenames = imageFilenames;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
