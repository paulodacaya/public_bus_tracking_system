package com.paulodacaya.bustrackingsystem.timetable;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Timetable {

  private final String mBaseURL;     // Base PTV URL
  private final String mPrivateKey;  // Private Key from PTV
  private final int mDeveloperId;    // Developer ID from PTV
  private String mUri;

  public Timetable() {
    mBaseURL = "http://timetableapi.ptv.vic.gov.au";
    mPrivateKey = "1a055809-d371-4953-b637-7db71b052303";
    mDeveloperId = 3000588;
  }


  /**
   * Method to demonstrate building of Timetable API URL
   *
   * mBaseURL            - Timetable API base URL without slash at the end ( Example: http://timetableapi.ptv.vic.gov.au )
   * mPrivateKey         - Developer Key supplied by PTV (((Example: "9c132d31-6a30-4cac-8d8b-8a1970834799")
   * mUri                - Request URI with parameters(Example: /v2/mode/0/line/8/stop/1104/directionid/0/departures/all/limit/5?for_utc=2014-08-15T06:18:08Z)
   * mDeveloperId        - Developer ID supplied by PTV
   * @return             - Complete URL with signature
   * @throws Exception   - DON'T FORGET TO HANDLE EXCEPTIONS
   **/

  // Build URI for stops near a specific location, max_result = 30, max_distance = 1000meters
  public void setNearLocationUri( double latitude, double longitude ) {
    mUri = "/v3/stops/location/" + latitude + "," + longitude + "?route_types=2&max_distance=1000";
  }

  // Build URI for all stops on a specific route
  public void  setAllStopsOnRouteUri( String routeId ) {
    mUri = "/v3/stops/route/" + routeId + "/route_type/2";
  }

  // Build URI for bus number search
  public void setBusNumberSearchUri( String busNumber ) {
    mUri = "/v3/search/" + busNumber + "?route_types=2";
  }

  // Build URI for departures for a specific route from a stop, with max result of 1.
  public void setDeparturesForRouteUri(String stopId, String routeId, String directionId , String fullUTC ) {

    //Convert fullUTC to a format that URI can read, replace ":" with "%3A".
    fullUTC = fullUTC.replaceAll( ":", "%3A" );

    mUri = "/v3/departures/route_type/2/stop/" + stopId + "/route/" + routeId
            + "?direction_id=" + directionId + "?date_utc=" + fullUTC + "&max_results=1";
  }

  // Build URI for departure time for a specific stop, with a max result of 8 (show follow 8 stops)
  public void setDeparturesForStopUri( String stopId, String fullUTC ) {

    //Convert fullUTC to a format that URI can read, replace ":" with "%3A".
    fullUTC = fullUTC.replaceAll( ":", "%3A" );

    mUri = "/v3/departures/route_type/2/stop/" + stopId + "?date_utc=" + fullUTC + "&max_results=8";
  }

  // Build URI for directions that a route travels in
  public void setRouteDirectionUri( String routeId ) {
    mUri =  "/v3/directions/route/" + routeId;
  }





  public String buildTTAPIURL() throws NoSuchAlgorithmException, InvalidKeyException {

      String HMAC_SHA1_ALGORITHM = "HmacSHA1";
      StringBuffer uriWithDeveloperID = new StringBuffer().append(mUri).append(mUri.contains("?") ? "&" : "?")
              .append("devid=" + mDeveloperId);
      byte[] keyBytes = mPrivateKey.getBytes();
      byte[] uriBytes = uriWithDeveloperID.toString().getBytes();
      Key signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
      byte[] signatureBytes = mac.doFinal(uriBytes);
      StringBuffer signature = new StringBuffer(signatureBytes.length * 2);
      for (byte signatureByte : signatureBytes) {
        int intVal = signatureByte & 0xff;
        if (intVal < 0x10) {
          signature.append("0");
        }
        signature.append(Integer.toHexString(intVal));
      }
      StringBuffer url = new StringBuffer(mBaseURL).append(mUri).append(mUri.contains("?") ? "&" : "?")
              .append("devid=" + mDeveloperId).append("&signature=" + signature.toString().toUpperCase());

      return url.toString();

  }
}
