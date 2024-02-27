package com.karag.civilprotectionapp.models;

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
}
