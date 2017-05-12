package com.sjsu.ten.sparkapp;

/**
 * Created by gagan on 12/29/2016.
 */
public class Data {

    private static Data ourInstance = null;


    public static Data getInstance() {
        if (ourInstance == null)
            ourInstance = new Data();
        return ourInstance;
    }

    private Data() {
    }

    private String garage;
    private String current="";
    private double Percent4th;
    private double Percent7th;
    private double Percent10th;

    public String getGarage(){
        return garage;
    }
    public void setGarage(String garage){
        this.garage = garage;
    }

    public String getCurrent() { return current; }
    public void setCurrent(String current) { this.current = current; }

    public double getPercent(String choice){
        if (choice.equals("4th"))
            return Percent4th;
        if (choice.equals("7th"))
            return Percent7th;
        if (choice.equals("10th"))
            return Percent10th;
        return 0;
    }

    public void setPercent4th(double Percent4th){
        this.Percent4th = Percent4th;
    }
    public void setPercent7th(double Percent7th){
        this.Percent7th = Percent7th;
    }
    public void setPercent10th(double Percent10th){
        this.Percent10th = Percent10th;
    }

}
