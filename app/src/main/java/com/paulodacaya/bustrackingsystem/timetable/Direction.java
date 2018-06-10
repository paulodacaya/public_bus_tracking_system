package com.paulodacaya.bustrackingsystem.timetable;

public class Direction {

  private int mDirectionId;
  private String mDirectionName;
  private int mRouteId;
  private String mBusNumber;

  public int getDirectionId() {
    return mDirectionId;
  }

  public void setDirectionId(int directionId) {
    mDirectionId = directionId;
  }

  public String getDirectionName() {
    return mDirectionName;
  }

  public void setDirectionName(String directionName) {
    mDirectionName = directionName;
  }

  public int getRouteId() {
    return mRouteId;
  }

  public void setRouteId(int routeId) {
    mRouteId = routeId;
  }

  public String getBusNumber() {
    return mBusNumber;
  }

  public void setBusNumber(String busNumber) {
    mBusNumber = busNumber;
  }
}
