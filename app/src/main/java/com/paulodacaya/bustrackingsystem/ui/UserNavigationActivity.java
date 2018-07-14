package com.paulodacaya.bustrackingsystem.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.adapters.StopAdapter;
import com.paulodacaya.bustrackingsystem.timetable.Stop;
import com.paulodacaya.bustrackingsystem.timetable.Stops;
import com.paulodacaya.bustrackingsystem.timetable.Timetable;
import com.paulodacaya.bustrackingsystem.ui.fragments.AlertDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  @BindView( R.id.toolbar ) Toolbar mToolbar;
  @BindView( R.id.drawer_layout ) DrawerLayout mDrawerLayout;
  @BindView( R.id.nav_view ) NavigationView mNavigationView;
  
  @BindView( R.id.contentTitle ) TextView mContentTitle;
  @BindView( R.id.subContentTitle ) TextView mSubContentTitle;
  
  @BindView( R.id.outOfProximityPrompt ) ImageView mOutOfProximityPrompt;
  @BindView( R.id.progressBar ) ProgressBar mProgressBar;
  
  @BindView( R.id.userHomeBody ) ConstraintLayout mHomeBody;
  @BindView( R.id.userTimetableInfoBody ) ConstraintLayout mTimetableInfoBody;

  @BindView( R.id.stopsDistanceRecyclerView ) RecyclerView mStopsDistanceRecyclerView;
  
  // Debugging TAG
  public static final String TAG = UserNavigationActivity.class.getSimpleName();

  private LocationManager mLocationManager;   // used to manage location
  private LocationListener mLocationListener; // handle location events

  private double mLatitude;
  private double mLongitude;

  private Stops mStops;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_navigation);
    ButterKnife.bind( this );

    setSupportActionBar(mToolbar);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    mNavigationView.setNavigationItemSelectedListener(this);

    //------------------------------------------------------------------------------------------
    // Action Bar Icon
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    actionBar.setIcon( R.drawable.pbt_logo_letters_white_small );
    actionBar.setDisplayShowTitleEnabled( false );
    //------------------------------------------------------------------------------------------

    toggleContent( R.id.nav_home ); // toggle to home content

    mLocationManager = (LocationManager) this.getSystemService( LOCATION_SERVICE ); // instantiate LocationManager
    mLocationListener = new LocationListener() {
      @Override
      public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
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

    // check if this app has existing permissions
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

      // it goes here if it doesn't have permission, so we request permission
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    } else {

      // we already had permission, so this block runs, requesting location updates with minimum delays
      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

      // get current location of device and put on map
      Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      mLatitude = location.getLatitude();
      mLongitude = location.getLongitude();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // this gets called after answering the dialog that pops up from a location request
    // it checks if the permission was granted, then location is requested

    if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();

        toggleContent( R.id.nav_home ); // toggle to home content
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_home) {
      toggleContent( R.id.nav_home );

    }
    else if (id == R.id.nav_timetable_information) {
      toggleContent( R.id.nav_timetable_information );

      // fetch/display data
      try {
        getNearbyStops(mLatitude, mLongitude);
      } catch ( NoSuchAlgorithmException error ) {
        Log.e(TAG, "NoSuchAlgorithmException caught: ", error);
      } catch ( InvalidKeyException error ) {
        Log.e(TAG, "InvalidKeyException caught: ", error);
      } catch ( Exception error ) {
        Log.e(TAG, "Exception caught: ", error);
      }


    }
    else if (id == R.id.nav_real_time) {
      toggleContent( R.id.nav_real_time );

    }
    else if (id == R.id.nav_exit) {
      startActivity( new Intent( UserNavigationActivity.this, AuthenticationActivity.class ));
      finish(); // close activity
    }

    mDrawerLayout.closeDrawer(GravityCompat.START); // close drawer
    return true;
  }

  private void toggleContent( int id ) {
    // toggle content depending on what was tab was clicked
    switch (id) {
      case R.id.nav_home:
        mContentTitle.setText( R.string.content_title_home_page );
        mSubContentTitle.setText( R.string.content_sub_title_home_page );

        mHomeBody.setVisibility( View.VISIBLE );
        mTimetableInfoBody.setVisibility( View.INVISIBLE );
        break;

      case R.id.nav_timetable_information:
        mContentTitle.setText( R.string.content_title_timetable_information );
        mSubContentTitle.setText( R.string.content_sub_title_timetable_information );
  
        mHomeBody.setVisibility( View.INVISIBLE );
        mTimetableInfoBody.setVisibility( View.VISIBLE );
        mProgressBar.setVisibility( View.INVISIBLE );
        mOutOfProximityPrompt.setVisibility( View.INVISIBLE );
        break;

      case R.id.nav_real_time:
        mContentTitle.setText( R.string.content_title_real_time  );
        mSubContentTitle.setText( R.string.content_sub_title_real_time );
  
        mHomeBody.setVisibility( View.INVISIBLE );
        mTimetableInfoBody.setVisibility( View.INVISIBLE );
        break;
    }
  }

  private void toggleProgressBar() {

    // toggle status of refresh depending on the status of progress bar
    if( mProgressBar.getVisibility() == View.INVISIBLE ) {
      mProgressBar.setVisibility( View.VISIBLE );
      mStopsDistanceRecyclerView.setVisibility( View.INVISIBLE );
      mOutOfProximityPrompt.setVisibility( View.INVISIBLE );
    } else {
      mProgressBar.setVisibility( View.INVISIBLE );
      mStopsDistanceRecyclerView.setVisibility( View.VISIBLE);
    }

  }

  private void getNearbyStops( double latitude, double longitude ) throws Exception {

    // Create SIGNED URL (PTV specifications)
    Timetable timetable = new Timetable();
    timetable.setNearLocationUri( latitude, longitude );
    String timetableUrl = timetable.buildTTAPIURL();
    
    if( isNetworkAvailable() ) {

      toggleProgressBar();
      
      OkHttpClient client = new OkHttpClient();
      // Build a request that the client will send to the server
      Request request = new Request.Builder().url(timetableUrl).build();
      Call call = client.newCall(request);

      // Execute on Thread
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              toggleProgressBar();
            }
          });

          alertUserAboutError();
        }

        @Override
        public void onResponse(Call call, Response response) {
          try {
            String jsonData = response.body().string();

            if (response.isSuccessful()) {

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  toggleProgressBar();
                }
              });

              mStops = parseStopsDetails(jsonData);

              // run on MAIN UI THREAD
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  updateStopsList();
                }
              });

            } else {
              alertUserAboutError(); // dialog box
            }
          } catch (IOException error) {
            Log.e(TAG, "IOException caught: ", error);
          } catch (JSONException error) {
            Log.e(TAG, "JSONException caught: ", error);
          }
        }
      });
    } else {
      Toast.makeText( this, R.string.network_unavailable_message, Toast.LENGTH_LONG ).show();
    }

  }

  private Stops parseStopsDetails( String jsonData ) throws JSONException {

    Stops stops = new Stops();
    stops.setStops( getStop( jsonData ) );

    return stops; //return object with complete object with parsed data from API
  }

  private Stop[] getStop( String jsonData ) throws JSONException {

    JSONObject stopsRootObject = new JSONObject( jsonData );
    JSONArray stopsArray = stopsRootObject.getJSONArray( "stops" );

    // If there is no objects stored in the array,
    // meaning there is not bus stops in proximity of 1km. Display 'no bus stop proximity image'.
    if( stopsArray.length() == 0 ) {

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mOutOfProximityPrompt.setVisibility( View.VISIBLE );
          mStopsDistanceRecyclerView.setVisibility( View.INVISIBLE );
        }
      });

      return null;

    } else {

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mOutOfProximityPrompt.setVisibility( View.INVISIBLE );
          mStopsDistanceRecyclerView.setVisibility( View.VISIBLE );
        }
      });
  
      // Create object array of Stop Objects
      Stop[] stops = new Stop[ stopsArray.length() ];
      
      for(int i = 0; i < stopsArray.length(); i++ ) {

        JSONObject jsonStop = stopsArray.getJSONObject( i );
        Stop stop = new Stop();

        stop.setStopDistance( jsonStop.getDouble( "stop_distance" ) );
        stop.setStopName( jsonStop.getString( "stop_name" ));
        stop.setStopId( jsonStop.getInt( "stop_id" ));
        stop.setStopLatitude( jsonStop.getDouble( "stop_latitude" ));
        stop.setStopLongitude( jsonStop.getDouble( "stop_longitude" ));
  
        // set Stop Object into Stops[] Array of Objects
        stops[i] = stop;
      }

      return stops;
    }
  }

  private void updateStopsList() {

    if( mStops.getStops() != null ) {

      StopAdapter adapter = new StopAdapter(this, mStops.getStops()); // make build adapter
      mStopsDistanceRecyclerView.setAdapter(adapter);                         // set adapter
      RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
      mStopsDistanceRecyclerView.setLayoutManager(layoutManager);
      mStopsDistanceRecyclerView.setHasFixedSize(true); // recommended to increase performance
    }
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
}
