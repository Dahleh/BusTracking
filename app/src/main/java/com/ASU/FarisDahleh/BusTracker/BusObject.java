package com.ASU.FarisDahleh.BusTracker;


public class BusObject {

    private String id;
    private String busNumber;
    private double longitude;
    private double latitude;
    private boolean EngineOn;
    private int speed;
    private boolean SOS;
    private boolean doorOpen;


    public BusObject() {
    }

    public boolean getSOS() {
        return SOS;
    }

    public void setSOS(boolean SOS) {
        this.SOS = SOS;
    }

    public boolean getdoorOpen() {
        return doorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public boolean getEngineOn() {
        return EngineOn;
    }

    public void setEngineOn(boolean EngineOn) {
        this.EngineOn = EngineOn;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
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
