package com.example.measure_movement;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class LocationEntity {


    private double latitude;

    private double longitude;

    private double altitude;

    @PrimaryKey
    private long time;

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String toString(){
        return "time: "+getTime()+" lat: "+getLatitude()+" long: "+getLongitude()+" alt: "+getAltitude();

    }
}
