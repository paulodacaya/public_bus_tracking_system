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
import android.widget.Toast;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.adapters.DirectionAdapter;
import com.paulodacaya.bustrackingsystem.timetable.Direction;
import com.paulodacaya.bustrackingsystem.timetable.Directions;
import com.paulodacaya.bustrackingsystem.timetable.Timetable;
import com.paulodacaya.bustrackingsystem.ui.fragments.AlertDialogFragment;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

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

public class RouteDirectionActivity extends AppCompatActivity {

  @BindView( R.id.routeDirectionRecyclerView ) RecyclerView mRouteDirectionRecyclerView;
  @BindView( R.id.progressBar ) ProgressBar mProgressBar;

  public static final String TAG = RouteDirectionActivity.class.getSimpleName(); //generate TAG

  private String mRouteId;
  private String mBusNumber;

  private Directions mDirections;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_route_direction);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    // get intent string values
    Intent intent = getIntent();
    mRouteId = intent.getStringExtra( Constants.ROUTE_ID );
    mBusNumber = intent.getStringExtra( Constants.BUS_NUMBER );
    //----------------------------------------------------------------------------------------------

    mProgressBar.setVisibility( View.INVISIBLE );

    // fetch route direction data
    try {
      getRouteDirections( mRouteId );
    } catch ( NoSuchAlgorithmException error ) {
      Log.e(TAG, "NoSuchAlgorithmException caught: ", error);
    } catch ( InvalidKeyException error ) {
      Log.e(TAG, "InvalidKeyException caught: ", error);
    } catch ( Exception error ) {
      Log.e(TAG, "Exception caught: ", error);
    }

  }

  private void toggleProgressBar() {

    // toggle status of refresh depending on the status of progress bar
    if( mProgressBar.getVisibility() == View.INVISIBLE ) {
      mProgressBar.setVisibility( View.VISIBLE );
      mRouteDirectionRecyclerView.setVisibility( View.INVISIBLE );
    } else {
      mProgressBar.setVisibility( View.INVISIBLE );
      mRouteDirectionRecyclerView.setVisibility( View.VISIBLE);
    }

  }

  private void getRouteDirections(String routeId) throws NoSuchAlgorithmException, InvalidKeyException {

    Timetable timetable = new Timetable();
    timetable.setRouteDirectionUri( mRouteId );
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

            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                toggleProgressBar();
              }
            });

            String jsonData = response.body().string(); //get JSON object
            Log.v(TAG, jsonData);

            if (response.isSuccessful()) {

              mDirections = parseDirectionDetails(jsonData); // parse JSON object from API :)

              // run on MAIN UI THREAD
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  updateDirectionList();
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

  private Directions parseDirectionDetails(String jsonData) throws JSONException {

    Directions directions  = new Directions();
    directions.setDirections( getDirection( jsonData ) );

    return directions; //return object with complete object with parsed data from API :)
  }

  private Direction[] getDirection(String jsonData) throws JSONException {

    JSONObject directionRootObject = new JSONObject( jsonData );
    JSONArray directionsArray = directionRootObject.getJSONArray( "directions" );

    Direction[] directions = new Direction[ directionsArray.length() ]; //create object array of Stop Objects

    // fill array
    for(int i = 0; i < directionsArray.length(); i++ ) {

      JSONObject jsonDirection = directionsArray.getJSONObject( i ); //loop through each json Object
      Direction direction = new Direction();

      direction.setDirectionId( jsonDirection.getInt( "direction_id" ) );
      direction.setDirectionName( jsonDirection.getString( "direction_name" ));
      direction.setRouteId( jsonDirection.getInt( "route_id" ));
      direction.setBusNumber( mBusNumber ); // pass bus number

      directions[i] = direction;
    }

    return directions;
  }

  private void updateDirectionList() {
    DirectionAdapter adapter = new DirectionAdapter( this, mDirections.getDirections() );
    mRouteDirectionRecyclerView.setAdapter( adapter );
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
    mRouteDirectionRecyclerView.setLayoutManager( layoutManager );
    mRouteDirectionRecyclerView.setHasFixedSize( true ); // recommended to increase performance
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
