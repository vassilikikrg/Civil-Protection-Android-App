package com.karag.civilprotectionapp.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Emergency {
    private String Name;
    private String GreekName;
    private int range; //in km
    private long timespan; //in hours

    public Emergency(String name, String greekName, int range, long timespan) {
        Name = name;
        GreekName = greekName;
        this.range = range;
        this.timespan = timespan;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getGreekName() {
        return GreekName;
    }

    public void setGreekName(String greekName) {
        GreekName = greekName;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public long getTimespan() {
        return timespan;
    }

    public void setTimespan(long timespan) {
        this.timespan = timespan;
    }

    public long timespanToMillieSeconds(){
        return this.timespan*60*60*1000;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> emergencyMap = new HashMap<>();
        emergencyMap.put("GreekName", this.getGreekName());
        emergencyMap.put("Name", this.getName());
        emergencyMap.put("range", this.getRange());
        emergencyMap.put("timespan", this.getTimespan());
        return emergencyMap;
    }
    public static Emergency documentToEmergency(QueryDocumentSnapshot document){
        // Extract data from Firestore document and create Emergency object
        String Name = document.getString("Name");
        String GreekName = document.getString("GreekName");
        Double rangeDb=document.getDouble("range");
        int range = rangeDb.intValue();
        long timespan = document.getLong("timespan");
        // Create and return the Incident object
        return new Emergency(Name,GreekName,range,timespan);
    }
}
