package com.floo.pedometer;

/**
 * Created by SONY_VAIO on 6/6/2015.
 */
public class OutdoorData {
    private int id;
    private String timeStamp;
    private int minutes;

    public OutdoorData(int id, String timeStamp, int minutes) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.minutes = minutes;
    }

    public OutdoorData(String timeStamp, int minutes) {
        this.timeStamp = timeStamp;
        this.minutes = minutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
