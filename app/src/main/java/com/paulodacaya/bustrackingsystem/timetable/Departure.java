package com.paulodacaya.bustrackingsystem.timetable;

public class Departure {

  private int mStopId;
  private int mRouteId;
  private String mRouteName;
  private int mRunId;
  private int mDirectionId;
  private  String mScheduledDeparture;

  public int getStopId() {
    return mStopId;
  }

  public void setStopId(int stopId) {
    mStopId = stopId;
  }

  public int getRouteId() {
    return mRouteId;
  }

  public void setRouteId(int routeId) {
    mRouteId = routeId;
  }

  public String getRouteName() {
    return mRouteName;
  }

  public void setRouteName(String routeName) {
    mRouteName = routeName;
  }

  public int getRunId() {
    return mRunId;
  }

  public void setRunId(int runId) {
    mRunId = runId;
  }

  public int getDirectionId() {
    return mDirectionId;
  }

  public void setDirectionId(int directionId) {
    mDirectionId = directionId;
  }

  public String getScheduledDeparture() {
    return mScheduledDeparture;
  }

  public void setScheduledDeparture(String scheduledDeparture) {
    mScheduledDeparture = scheduledDeparture;
  }
}
