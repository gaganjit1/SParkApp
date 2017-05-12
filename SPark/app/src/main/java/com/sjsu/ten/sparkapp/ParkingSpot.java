package com.sjsu.ten.sparkapp;

/**
 * Created by Ben on 11/16/2016.
 */

public class ParkingSpot {
    private String id;
    private String status;
    private int time;
    private int paid;
    private int viewers;
    private int floor;
    private String current;

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid() {
        this.paid = 1;
    }

    public int getViewers() {
        return viewers;
    }

    public void setViewers() {
        this.viewers += 1;
    }

    public int getFloor(){
        return floor;
    }

    public void setFloor(int floor){
        this.floor = floor;
    }


}

