package com.karag.civilprotectionapp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprovedIncident {

    private String id;

    private String description;

    private List<String> imageFilenames;
    private double dangerLevel;
    private Date datetime;
    private String emergencyType;
    private double latitude;
    private double longitude;
    private double numOfReports;
    private double range;

    public ApprovedIncident(String description, String emergencyType, List<String> imageFilenames, Date datetime, double longitude, double latitude) {
        this.description = description;
        this.emergencyType = emergencyType;
        this.imageFilenames = imageFilenames;
        this.datetime = datetime;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public ApprovedIncident(String id, double dangerLevel, Date datetime, String emergencyType, double latitude, double longitude, double numOfReports, double range) {
        this.id = id;
        this.dangerLevel = dangerLevel;
        this.datetime = datetime;
        this.emergencyType = emergencyType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numOfReports = numOfReports;
        this.range = range;
    }
    public ApprovedIncident(double dangerLevel, Date datetime, String emergencyType, double latitude, double longitude, double numOfReports, double range) {
        this.dangerLevel = dangerLevel;
        this.datetime = datetime;
        this.emergencyType = emergencyType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.numOfReports = numOfReports;
        this.range = range;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(double dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public double getNumOfReports() {
        return numOfReports;
    }

    public void setNumOfReports(double numOfReports) {
        this.numOfReports = numOfReports;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
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

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> approvedIncident = new HashMap<>();
        approvedIncident.put("emergencyType", this.getEmergencyType());
        approvedIncident.put("dangerLevel", this.getDangerLevel());
        approvedIncident.put("numOfReports", this.getNumOfReports());
        approvedIncident.put("dateTime", this.getDatetime());
        approvedIncident.put("latitude", this.getLatitude());
        approvedIncident.put("longitude", this.getLongitude());
        approvedIncident.put("range",this.getRange());

        return approvedIncident;
    }

    public static ApprovedIncident documentToIncident(QueryDocumentSnapshot document){
        // Extract data from Firestore document and create ApprovedIncident object
        String id= document.getId();
        String emergencyType = document.getString("emergencyType");
        Date datetime = document.getDate("dateTime");
        double latitude = document.getDouble("latitude");
        double longitude = document.getDouble("longitude");
        double dangerLevel=document.getDouble("dangerLevel");
        double numOfReports=document.getDouble("numOfReports");
        Double range=document.getDouble("range");
        // If range is null, assign the default value of 10
        if (range == null) {
            range = 10.0; // Default value
        }
        // Create and return the Incident object
        return new ApprovedIncident(id,dangerLevel,datetime, emergencyType,latitude, longitude,numOfReports,range);
    }
}
