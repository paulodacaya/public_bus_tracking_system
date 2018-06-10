package com.paulodacaya.bustrackingsystem.database;

public class StopData {

  private int mId;
  private String mDate;
  private String mStopName;
  private String mScheduledTime;
  private String mArrivalTime;
  private long mDelta;
  private float mDeltaMinSec;

  public StopData() {
  }

  public StopData(String date, String stopName, String scheduledTime, String arrivalTime, long delta) {
    mDate = date;
    mStopName = stopName;
    mScheduledTime = scheduledTime;
    mArrivalTime = arrivalTime;
    mDelta = delta;
  }

  public int getId() {
    return mId;
  }

  public void setId(int id) {
    mId = id;
  }

  public String getDate() {
    return mDate;
  }

  public void setDate(String date) {
    mDate = date;
  }

  public String getStopName() {
    return mStopName;
  }

  public void setStopName(String stopName) {
    mStopName = stopName;
  }

  public String getScheduledTime() {
    return mScheduledTime;
  }

  public void setScheduledTime(String scheduledTime) {
    mScheduledTime = scheduledTime;
  }

  public String getArrivalTime() {
    return mArrivalTime;
  }

  public void setArrivalTime(String arrivalTime) {
    mArrivalTime = arrivalTime;
  }

  public long getDelta() {
    return mDelta;
  }

  public void setDelta(long delta) {
    mDelta = delta;
  }

  public float getDeltaMinSec() {
    return mDeltaMinSec;
  }

  public void setDeltaMinSec(float deltaMinSec) {
    mDeltaMinSec = deltaMinSec;
  }
}
