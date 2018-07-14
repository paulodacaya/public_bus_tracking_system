package com.paulodacaya.bustrackingsystem.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.database.DatabaseHandler;
import com.paulodacaya.bustrackingsystem.database.StopData;
import com.paulodacaya.bustrackingsystem.timetable.Stop;
import com.paulodacaya.bustrackingsystem.timetable.Timetable;
import com.paulodacaya.bustrackingsystem.ui.fragments.AlertDialogFragment;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DriverMapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

  public static final String TAG = DriverMapsActivity.class.getSimpleName(); //generate TAG

  @BindView( R.id.busNumberLabel ) TextView mBusNumberLabel;
  @BindView( R.id.stopNameLabel ) TextView mStopNameLabel;
  @BindView( R.id.endShiftButton ) Button mEndShiftButton;
  @BindView( R.id.deltaValue ) TextView mDeltaValueLabel;

  private GoogleMap mMap;
  private LocationManager mLocationManager;   // used to manage location
  private LocationListener mLocationListener; // handle location events
  private Marker mCurrentLocationMarker;

  private String mRouteId;
  private String mDirectionId;
  private String mBusNumber;
  private String mStopId;
  private String mPrevStopId = "empty"; // prevent multiple data storage

  private String mScheduledUTC;
  private String mScheduledUTCTime;
  private String mCurrentUTCDate;
  private String mCurrentUTCTime;

  private int mTotalOnTimeTaps = 0;
  private int mTotalTaps = 1; // cannot divide by zero
  private boolean mIsFirstTapped = false;

  // DATABASE
  DatabaseHandler mDataBase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_driver_maps);
    ButterKnife.bind( this );

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);

    mapFragment.getMapAsync(this);

    //----------------------------------------------------------------------------------------------
    // get intent string values
    Intent intent = getIntent();
    mRouteId = intent.getStringExtra( Constants.ROUTE_ID );
    mBusNumber = intent.getStringExtra( Constants.BUS_NUMBER );
    mDirectionId = intent.getStringExtra( Constants.DIRECTION_ID );
    mBusNumberLabel.setText( "Bus " + mBusNumber );  // set bus number label
    //----------------------------------------------------------------------------------------------

    // create database object with table name
    // NOTE: IF database already exist: new table will NOT be created.
    final String tableName = "Bus_" + mBusNumber;
    mDataBase = new DatabaseHandler( this, tableName );

    // Dynamically create table if route has not been selected
    if( !mDataBase.isTableNameExist( tableName ) ) {
      mDataBase.createTable( tableName );
    }

    try {
      getBusRouteStopLocations();
    } catch (NoSuchAlgorithmException error) {
      error.printStackTrace();
    } catch (InvalidKeyException error) {
      error.printStackTrace();//
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    /**
     * During Driver shift, only need writable access.
     * onResume() gets called after onCreate() and when the activity is re-entered.
     */
    mDataBase.openWritableAccess();
  }

  @Override
  protected void onPause() {
    super.onPause();

    mDataBase.closeWritableAccess();
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    mLocationManager = (LocationManager) this.getSystemService( LOCATION_SERVICE );
    mMap.setOnMarkerClickListener( this );
    
    mLocationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
  
        /**
         * Location updates are disabled to test marker clicks
         * Uncommenting will constantly update position for real-time testing
         * */
        
        // mCurrentLocationMarker.remove();
        // setCurrentLocationOnMap( location );
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {

      }

      @Override
      public void onProviderEnabled(String provider) {

      }

      @Override
      public void onProviderDisabled(String provider) {

      }
    };

    
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    } else {
      
      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
      
      Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      setCurrentLocationOnMap( location );
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setCurrentLocationOnMap( location );
      }
    }
  }

  private void setCurrentLocationOnMap( Location location ) {
    
    
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    // add marker to map and also return that marker to field variable so it can be removed and updated
    mCurrentLocationMarker = mMap.addMarker( new MarkerOptions().position( latLng )
            .icon(BitmapDescriptorFactory.fromResource( R.drawable.map_bus_icon ) )
            .title("Currently Location") );

    // keeps the map locked into the location of user during a location change
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
  }

  private void getBusRouteStopLocations() throws NoSuchAlgorithmException, InvalidKeyException {

    // create SIGNED URI (PTV specifications)
    Timetable timetable = new Timetable();
    timetable.setAllStopsOnRouteUri( mRouteId );
    String timetableUrl = timetable.buildTTAPIURL();
    Log.v(TAG, timetableUrl);

    final Stop stop = new Stop();
    
    if( isNetworkAvailable() ) {

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(timetableUrl).build();
      Call call = client.newCall(request);
      
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          alertUserAboutError(); // alert dialog
        }

        @Override
        public void onResponse(Call call, Response response) {
          try {
            String jsonData = response.body().string(); // get JSON object

            if (response.isSuccessful()) {

              JSONObject rootObject = new JSONObject( jsonData );
              JSONArray stopsArray = rootObject.getJSONArray( "stops" );
              
              for( int i = 0; i < stopsArray.length(); i++ ) {
                JSONObject stopObj = stopsArray.getJSONObject( i );

                // get co-ordinates
                final double lat = stopObj.getDouble( "stop_latitude" );
                final double lon = stopObj.getDouble( "stop_longitude" );

                // assign information to stopObj object
                stop.setStopName( stopObj.getString( "stop_name" ));
                stop.setStopId( stopObj.getInt( "stop_id" ));
                stop.setStopLatitude( lat );
                stop.setStopLongitude( lon );


                // create Marker for location
                final MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker( BitmapDescriptorFactory.HUE_ORANGE ));
                markerOptions.title( stop.getStopName() );

                markerOptions.position( new LatLng( lat, lon ) ); // set location of marker from OBJ
                markerOptions.snippet( stop.getStopId() + "" );   // convert int to string

                // Add circle around marker for better visual
                final CircleOptions circleOptions = new CircleOptions();
                circleOptions.center( new LatLng( stop.getStopLatitude(), stop.getStopLongitude() ));
                circleOptions.radius( 11 );
                circleOptions.strokeWidth( 2f );
                circleOptions.fillColor( Color.GRAY );
                
                // run Google Map task on main UI thread
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Marker marker = mMap.addMarker( markerOptions );
                    mMap.addCircle( circleOptions );
                  }
                });

              }

            } else {
              alertUserAboutError(); // dialog box
            }

          } catch (IOException error) {
            Log.e(TAG, "IOException caught: ", error);
          } catch (JSONException error ) {
            Log.e(TAG, "JSONException caught: ", error);
          }
        }
      });
    } else {
      Toast.makeText( this, R.string.network_unavailable_message, Toast.LENGTH_LONG ).show();
    }
  }

  @Override
  public boolean onMarkerClick(Marker marker) {

    mStopId = marker.getSnippet();               // get stopId from marker
    mStopNameLabel.setText( marker.getTitle() ); // set label to stop name

    // Make sure data is not recorded twice
    if( mStopId.equals(mPrevStopId) ) {
      Toast.makeText( getApplicationContext(), "Already pressed, Thank you for making sure!", Toast.LENGTH_SHORT ).show();
    } else {

      mPrevStopId = mStopId; // click on the same marker

      mCurrentUTCDate = getCurrentUTCDate(); // get current date when marker is clicked (UTC format)
      mCurrentUTCTime = getCurrentUTCTime(); // get current time when marker is clicked (UTC format)

      // Store data to database
      try {
        storeToDatabase();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      }

    }

    return false;
  }

  public String getCurrentUTCDate(){
    Date time = Calendar.getInstance().getTime();

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

    return dateFormatter.format(time);
  }

  public String getCurrentUTCTime(){
    Date time = Calendar.getInstance().getTime();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

    return dateFormatter.format(time);
  }

  public long getCurrentUTCTimeMillis() {
    return System.currentTimeMillis();
  }

  public long getUTCTimeMillis( String timeStamp ) throws ParseException {

    timeStamp = timeStamp.replaceAll( "T", " " );
    timeStamp = timeStamp.replaceAll( "Z", "" );

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));

    Date date = format.parse(timeStamp); // use passed timeStamp and get value in milliseconds

    long millis = date.getTime(); // get milliseconds

    return millis;
  }

  private void storeToDatabase() throws NoSuchAlgorithmException, InvalidKeyException {

    // combine date and time for a valid URI request
    String fullUTC = mCurrentUTCDate + "T" + mCurrentUTCTime + "Z";

    // create SIGNED URI (PTV specifications)
    Timetable timetable = new Timetable();
    timetable.setDeparturesForRouteUri( mStopId, mRouteId, mDirectionId, fullUTC );
    String timetableUrl = timetable.buildTTAPIURL();

    // handle network requests
    if( isNetworkAvailable() ) {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(timetableUrl).build();
      Call call = client.newCall(request);

      //execute method on the call class that will return a response object
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          alertUserAboutError(); // alert dialog
        }

        @Override
        public void onResponse(Call call, Response response) {

          try {
            String jsonData = response.body().string(); // get JSON object

            if (response.isSuccessful()) {

              /** STORING DATA */
              //------------------------------------------------------------------------------------
              // get scheduled time at a specific bus stop at a specific time
              mScheduledUTC = getScheduledUTC( jsonData );

              // get current and schedule date in milliseconds
              long currentTimeUTCMillis = getCurrentUTCTimeMillis();
              long scheduledTimeUTCMillis = getUTCTimeMillis( mScheduledUTC );

              // get scheduled UTC time
              mScheduledUTCTime = getScheduledUTCTime( mScheduledUTC );

              // Calculate delta
              long deltaMillis = currentTimeUTCMillis - scheduledTimeUTCMillis;

              // Get time seconds, absolute value to remove negative sign
              final int deltaSeconds = Math.abs( (int) ( ( deltaMillis/1000)%60 ) );

              // Get time in minutes
              final long deltaMinutes = ( (deltaMillis / 1000)-deltaSeconds )/60;

              // Get total time
              final float deltaTotal = Float.parseFloat( deltaMillis + "." + deltaSeconds );

              // Create object to store data in SQLite database
              StopData stopData = new StopData(mCurrentUTCDate, mStopNameLabel.getText().toString(),
                      mScheduledUTCTime, mCurrentUTCTime, deltaMillis);

              mDataBase.addBusStopData( stopData ); // add data to database

              // update main UI with latest information
              runOnUiThread(new Runnable() {
                @Override
                public void run() {

                  if( !mIsFirstTapped )
                    mIsFirstTapped = true;
                  else
                    mTotalTaps++;

                  // update UI with delta information
                  mDeltaValueLabel.setText( deltaMinutes + ":" + deltaSeconds );

                  // set color depending if 'on time', 5 minute clearance
                  if( deltaTotal >= -5 && deltaTotal <= 5 ) {
                    mDeltaValueLabel.setTextColor(Color.GREEN); // ON TIME
                    mTotalOnTimeTaps++;
                  } else
                    mDeltaValueLabel.setTextColor(Color.RED);   // NOT ON TIME

                  // show successful message
                  Snackbar.make( findViewById(R.id.mapConstraintLayout ),
                          "Saved, Thank you for your assistance!",
                          Snackbar.LENGTH_LONG ).show();
                }
              });
              //------------------------------------------------------------------------------------

            } else {
              alertUserAboutError(); // dialog box
            }

          } catch (IOException error) {
            Log.e(TAG, "IOException caught: ", error);
          } catch (JSONException error ) {
            Log.e(TAG, "JSONException caught: ", error);
          } catch (ParseException error ) {
            Log.e(TAG, "ParseException caught: ", error);
          }
        }
      });
    } else {
      Toast.makeText( this, R.string.network_unavailable_message, Toast.LENGTH_LONG ).show();
    }

  }

  private String getScheduledUTC( String jsonData ) throws JSONException {

    JSONObject rootObj = new JSONObject( jsonData );
    JSONArray departureArray = rootObj.getJSONArray( "departures" );

    // get first obj in array
    JSONObject departureObj = departureArray.getJSONObject( 0 );

    return departureObj.getString( "scheduled_departure_utc" );
  }

  private String getScheduledUTCTime(String scheduledUTC) {

    String scheduledTime = scheduledUTC.substring(
            scheduledUTC.indexOf( 'T' ) + 1,
            scheduledUTC.indexOf( 'Z' )
    );

    return scheduledTime;
  }

  private boolean isNetworkAvailable() {

    // create manager
    ConnectivityManager manager = (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE );

    // network info object, this needs permissions to be successful ( Manifest ).
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();

    boolean isAvailable = false;

    // check if networkInfo has been assigned by checking null state
    // and if it is actually connected to the web
    if( networkInfo != null && networkInfo.isConnected() )
      isAvailable = true;

    return  isAvailable;
  }

  private void alertUserAboutError() {
    AlertDialogFragment dialog = new AlertDialogFragment(); // instantiate AlertDialogFragment
    dialog.show( getFragmentManager(), "error_dialog" ); // display it
  }

  @OnClick ( R.id.endShiftButton )
  public void startEndShiftActivity() {
    Intent intent = new Intent( DriverMapsActivity.this, EndShiftActivity.class );
    intent.putExtra( Constants.BUS_NUMBER, mBusNumber );
    intent.putExtra( Constants.TOTAL_ON_TIME_TAPS, mTotalOnTimeTaps );
    intent.putExtra( Constants.TOTAL_TAPS, mTotalTaps );
    startActivity( intent );
    finish();
  }
}
