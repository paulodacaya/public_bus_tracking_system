package com.paulodacaya.bustrackingsystem.utilities;

import java.math.BigDecimal;

/**
  This class is used to reduce that amount of repeated code and type errors
  Where common constants, variables and methods can be defined that are used throughout
  the application.
**/

public class Constants {

  // General/Intent Constants
  public static final String ROUTE_ID = "routeId";
  public static final String DIRECTION_ID = "directionId";
  public static final String BUS_NUMBER = "busNumber";
  public static final String BUS_ROUTE = "busRoute";
  public static final String ROW_ID = "rowId";
  public static final String STOP_ID = "stopId";
  public static final String STOP_NAME = "stopName";
  public static final String TOTAL_TAPS = "totalTaps";
  public static final String TOTAL_ON_TIME_TAPS = "totalOnTimeTaps";

  // Database Constants
  public static final int DB_VERSION = 1;
  public static final String DB_NAME = "publicBusTrackerDB";

  // Database Column Key Names
  public static final String KEY_TABLE_NAME = "name";
  public static final String KEY_ID = "id";
  public static final String KEY_DATE = "date";
  public static final String KEY_STOP_NAME = "stop_name";
  public static final String KEY_SCHEDULED_TIME = "scheduled_time";
  public static final String KEY_ARRIVAL_TIME = "arrival_time";
  public static final String KEY_DELTA = "delta_in_ms";

  // Line Chart constants
  public static final float TEXT_SIZE = 12f;
  public static final float TEXT_SIZE_LARGE = 16f;


  // round a float value
  /***************************************************************************************
   *    Title: (STACK OVERFLOW) What's the best practice to round a float to 2 decimals?
   *    Author: Jav_Rock
   *    Date: answered Jan 18 '12 at 14:21
   *    Code version: --
   *    Availability: --
   ***************************************************************************************/
  public static float round(float decimal, int decimalPlace) {

    BigDecimal bigDecimal = new BigDecimal(Float.toString(decimal));

    bigDecimal = bigDecimal.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);

    return bigDecimal.floatValue();
  }
}
