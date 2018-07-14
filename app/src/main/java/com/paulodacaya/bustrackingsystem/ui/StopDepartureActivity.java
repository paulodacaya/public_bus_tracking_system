package com.paulodacaya.bustrackingsystem.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.adapters.DepartureAdapter;
import com.paulodacaya.bustrackingsystem.timetable.Departure;
import com.paulodacaya.bustrackingsystem.timetable.Departures;
import com.paulodacaya.bustrackingsystem.timetable.Timetable;
import com.paulodacaya.bustrackingsystem.ui.fragments.AlertDialogFragment;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StopDepartureActivity extends AppCompatActivity {

  @BindView( R.id.departureRecyclerView ) RecyclerView mDepartureRecyclerView;
  @BindView( R.id.progressBar ) ProgressBar mProgressBar;
  @BindView( R.id.stopNameLabel ) TextView mStopNameLabel;

  public static final String TAG = StopDepartureActivity.class.getSimpleName(); //generate TAG

  String mStopId;
  Departures mDepartures;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stop_departure);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    Intent intent = getIntent();
    mStopId = intent.getStringExtra( Constants.STOP_ID );
    String stopName = intent.getStringExtra( Constants.STOP_NAME );
    //----------------------------------------------------------------------------------------------
    mStopNameLabel.setText( stopName ); // set stopName

    mProgressBar.setVisibility( View.INVISIBLE );

    // display timetable corresponding to stop location
    try {
      getDepartureDetails();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private void getDepartureDetails() throws InvalidKeyException, NoSuchAlgorithmException {

    String currentUTCDate = getCurrentUTCDate(); // get current date (UTC format)
    String currentUTCTime = getCurrentUTCTime(); // get current time (UTC format)
    String fullUTC = currentUTCDate + "T" + currentUTCTime + "Z"; // create string to pass through

    // create SIGNED URI (PTV specifications)
    Timetable timetable = new Timetable();
    timetable.setDeparturesForStopUri( mStopId, fullUTC );
    String timetableUrl = timetable.buildTTAPIURL();

    if( isNetworkAvailable() ) {

      toggleProgressBar();

      OkHttpClient client = new OkHttpClient(); // use Third Party
      Request request = new Request.Builder().url(timetableUrl).build(); // build a request that the client will send to the server
      Call call = client.newCall(request); // After building the request, put it into the call object

      //execute method on the call class that will return a response object
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              toggleProgressBar();
            }
          });

          alertUserAboutError(); // alert dialog
        }

        @Override
        public void onResponse(Call call, Response response) {
          try {
            String jsonData = response.body().string(); //get JSON object

            if (response.isSuccessful()) {

              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  toggleProgressBar();
                }
              });

              mDepartures = parseDepartureDetails(jsonData); // parse JSON object from API :)

              // run on MAIN UI THREAD
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  updateDepartureList();
                }
              });

            } else {
              alertUserAboutError(); // dialog box
            }
          } catch (IOException error) {
            Log.e(TAG, "IOException caught: ", error);
          } catch (JSONException error) {
            Log.e(TAG, "JSONException caught: ", error);
          } catch (ParseException error) {
            Log.e(TAG, "ParseException caught: ", error);
          }
        }
      });
    } else {
      Toast.makeText( this, R.string.network_unavailable_message, Toast.LENGTH_LONG ).show();
    }
  }

  private Departures parseDepartureDetails(String jsonData) throws JSONException, ParseException {

    Departures departures = new Departures();

    departures.setDepartures( getDepartures( jsonData ) );

    return departures;
  }

  private Departure[] getDepartures( String jsonData ) throws JSONException, ParseException {

    JSONObject departuresRootObject = new JSONObject( jsonData );
    JSONArray departuresArray = departuresRootObject.getJSONArray( "departures" );

    Departure[] departures = new Departure[ departuresArray.length() ];

    // fill array with API details
    for( int i = 0; i < departuresArray.length(); i++ ) {

      JSONObject jsonDeparture = departuresArray.getJSONObject( i );

      Departure departure = new Departure();

      departure.setStopId( jsonDeparture.getInt( "stop_id" ));
      departure.setRouteId( jsonDeparture.getInt( "route_id" ));
      departure.setRunId( jsonDeparture.getInt( "run_id" ));
      departure.setScheduledDeparture(
              getScheduledTime( convertUTCtoAEST( jsonDeparture.getString( "scheduled_departure_utc" ) ) )
      );


      departures[i] = departure;
    }

    return departures;
  }

  private void updateDepartureList() {

    DepartureAdapter adapter = new DepartureAdapter( this, mDepartures.getDepartures() );
    mDepartureRecyclerView.setAdapter( adapter );
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
    mDepartureRecyclerView.setLayoutManager( layoutManager );
    mDepartureRecyclerView.setHasFixedSize( true );
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

  // COVERT UTC (Universal Time Coordinated) TO AEST (Australian Eastern Standard Time)
  public String convertUTCtoAEST( String fullUTC ) throws ParseException {

    /***************************************************************************************
     *    Title: (STACK OVERFLOW) Java: How do you convert a UTC timestamp to local time?
     *    Author: Steve Kuo
     *    Date: answered answered Sep 19 '12 at 1:12
     *    Code version: --
     *    Availability: --
     ***************************************************************************************/

    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    Date date = utcFormat.parse( fullUTC );

    // Convert to Australia/Melbourne Time
    DateFormat aetFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    aetFormat.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));

    return aetFormat.format(date);
  }

  private String getScheduledTime( String scheduledTime ) {

    String formattedTime = scheduledTime.substring(
            scheduledTime.indexOf( 'T' ) + 1,
            scheduledTime.indexOf( 'Z' )
    );

    String converted = "";

    // covert to 12 hour time
    try {
      final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
      final Date dateObj = sdf.parse( formattedTime );

      converted = new SimpleDateFormat("K:mm a").format(dateObj);

    } catch (final ParseException e) {
      e.printStackTrace();
    }

    return converted;
  }

  private void toggleProgressBar() {

    // toggle status of refresh depending on the status of progress bar
    if( mProgressBar.getVisibility() == View.INVISIBLE ) {
      mProgressBar.setVisibility( View.VISIBLE );
      mDepartureRecyclerView.setVisibility( View.INVISIBLE );
    } else {
      mProgressBar.setVisibility( View.INVISIBLE );
      mDepartureRecyclerView.setVisibility( View.VISIBLE );
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
