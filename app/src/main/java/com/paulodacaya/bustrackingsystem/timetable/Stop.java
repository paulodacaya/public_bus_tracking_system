package com.paulodacaya.bustrackingsystem.timetable;

public class Stop {

  private double mStopDistance;
  private String mStopName;
  private int mStopId;
  private double mStopLatitude;
  private double mStopLongitude;

  public int getStopDistance() {
    return (int)Math.round(mStopDistance); // round distance to whole number
  }

  public void setStopDistance(double stopDistance) {
    mStopDistance = stopDistance;
  }

  public String getStopName() {
    return mStopName;
  }

  public void setStopName(String stopName) {
    mStopName = stopName;
  }

  public int getStopId() {
    return mStopId;
  }

  public void setStopId(int stopId) {
    mStopId = stopId;
  }

  public double getStopLatitude() {
    return mStopLatitude;
  }

  public void setStopLatitude(double stopLatitude) {
    mStopLatitude = stopLatitude;
  }

  public double getStopLongitude() {
    return mStopLongitude;
  }

  public void setStopLongitude(double stopLongitude) {
    mStopLongitude = stopLongitude;
  }

}
