package com.karag.civilprotectionapp;


import com.google.firebase.Timestamp;

public class Incident {

    private String userId;
    private String description;
    private String emergencyType;
    private String imageFilename;
    private String username; // Added field for username
    private String location; // Added field for location
    private String datetime; // Changed type to Timestamp for datetime

    private double longitude;
    private double latitude;


    public Incident() {
        // Default constructor required for Firestore
    }

    public Incident(String userId, String description, String emergencyType, String imageFilename, String datetime, String location, double longitude, double latitude) {
        this.userId = userId;
        this.description = description;
        this.emergencyType = emergencyType;
        this.imageFilename = imageFilename;
        this.datetime = datetime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;

    }




    // Getters and setters
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

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
