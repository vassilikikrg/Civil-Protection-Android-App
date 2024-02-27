package com.karag.civilprotectionapp.models;

import java.util.Date;

public class MyIncident {
    private String id;
    private String userId;
    private String description;
    private String emergencyType;
    private String imageFilename;
    private Date datetime;
    private double latitude;
    private double longitude;
    private String status;


    public MyIncident(String id,String userId, String description, String emergencyType, String imageFilename, Date datetime,double latitude,double longitude) {
        this.id=id;
        this.userId = userId;
        this.description = description;
        this.emergencyType = emergencyType;
        this.imageFilename = imageFilename;
        this.datetime = datetime;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public MyIncident(String id, String userId, String description, String emergencyType, String imageFilename, Date datetime, double latitude, double longitude, String status) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.emergencyType = emergencyType;
        this.imageFilename = imageFilename;
        this.datetime = datetime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
