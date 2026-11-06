package com.example.listprocessor;

public class Cruise {
    private String cruiseLine, shipName, code, region;
    private int length;
    private double price, gratuity;

    public String getCruiseLine() { return cruiseLine; }
    public void setCruiseLine(String value) { cruiseLine = value; }
    public String getShipName() { return shipName; }
    public void setShipName(String value) { shipName = value; }
    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
    public String getRegion() { return region; }
    public void setRegion(String value) { region = value; }
    public int getLength() { return length; }
    public void setLength(int value) { length = value; }
    public double getPrice() { return price; }
    public void setPrice(double value) { price = value; }
    public double getGratuity() { return gratuity; }
    public void setGratuity(double value) { gratuity = value; }
    public double getTotalGratuity() { return gratuity * length; }
    public double getTotalCost() { return price + getTotalGratuity(); }
}
