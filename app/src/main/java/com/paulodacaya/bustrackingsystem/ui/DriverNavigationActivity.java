package com.paulodacaya.bustrackingsystem.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.adapters.SearchAdapter;
import com.paulodacaya.bustrackingsystem.timetable.Route;
import com.paulodacaya.bustrackingsystem.timetable.Search;
import com.paulodacaya.bustrackingsystem.timetable.Timetable;
import com.paulodacaya.bustrackingsystem.ui.fragments.AlertDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DriverNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

  @BindView( R.id.toolbar ) Toolbar mToolbar;
  @BindView( R.id.drawer_layout ) DrawerLayout mDrawerLayout;
  @BindView( R.id.nav_view ) NavigationView mNavigationView;
  
  @BindView( R.id.contentTitle ) TextView mContentTitle;
  @BindView( R.id.subContentTitle ) TextView mSubContentTitle;
  @BindView( R.id.notFoundPromptImage ) ImageView mNotFoundPromptImage;
  @BindView( R.id.progressBar ) ProgressBar mProgressBar;
  @BindView( R.id.driverHomeBody ) ConstraintLayout mDriverHomeBody;
  @BindView( R.id.driverShiftBody ) ConstraintLayout mDriverShiftBody;
  
  @BindColor( R.color.colorDefaultText ) int defaultText;
  @BindColor( R.color.colorPrimary ) int primary;
  @BindColor( R.color.colorAccent ) int accent;
  @BindColor( R.color.colorAccentDark ) int accentDark;
  
  @BindView( R.id.searchRecyclerView ) RecyclerView mSearchRecyclerView;
  @BindView( R.id.searchText ) EditText mSearchText;

  

  public static final String TAG = DriverNavigationActivity.class.getSimpleName();

  private Search mSearch;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_driver_navigation);
    ButterKnife.bind( this );

    setSupportActionBar(mToolbar);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    mNavigationView.setNavigationItemSelectedListener(this);
    
    // Action bar icon
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    actionBar.setIcon( R.drawable.pbt_logo_letters_white_small );
    actionBar.setTitle( "" );
  
    // Toggle to home content display
    toggleContent( R.id.nav_home );
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
    int id = item.getItemId();

    if (id == R.id.nav_home) {
      toggleContent( R.id.nav_home );

    } else if (id == R.id.nav_my_shift) {
      toggleContent( R.id.nav_my_shift );

    } else if (id == R.id.nav_exit) {
      startActivity( new Intent( DriverNavigationActivity.this, AuthenticationActivity.class ));
      finish();
    }

    mDrawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  private void toggleContent( int id ) {
    
    switch (id) {
      case R.id.nav_home:
        mContentTitle.setText( R.string.content_title_home_page );
        mContentTitle.setTextColor( defaultText );
        mContentTitle.setBackgroundColor( accentDark );
        
        mSubContentTitle.setText( R.string.content_sub_title_home_page );
        mSubContentTitle.setTextColor( accentDark );
        mSubContentTitle.setBackgroundColor( accent );

        mDriverHomeBody.setVisibility( View.VISIBLE );
        mDriverShiftBody.setVisibility( View.INVISIBLE );
        break;

      case R.id.nav_my_shift:
        mContentTitle.setText( R.string.content_title_my_shift );
        mContentTitle.setTextColor( accent );
        mContentTitle.setBackgroundColor( primary );
        
        mSubContentTitle.setText( R.string.content_sub_title_my_shift );
        mSubContentTitle.setTextColor( accent );
        mSubContentTitle.setBackgroundColor( primary );
        
        mProgressBar.setVisibility( View.INVISIBLE );
        mNotFoundPromptImage.setVisibility( View.INVISIBLE );
  
        mDriverHomeBody.setVisibility( View.INVISIBLE );
        mDriverShiftBody.setVisibility( View.VISIBLE );
        break;
    }
  }

  private void toggleProgressBar() {

    // toggle status of refresh depending on the status of progress bar
    if( mProgressBar.getVisibility() == View.INVISIBLE ) {
      mProgressBar.setVisibility( View.VISIBLE );
      mSearchRecyclerView.setVisibility( View.INVISIBLE );
      mNotFoundPromptImage.setVisibility( View.INVISIBLE );
    } else {
      mProgressBar.setVisibility( View.INVISIBLE );
      mSearchRecyclerView.setVisibility( View.VISIBLE);
    }

  }

  // handle search button click
  @OnClick( R.id.searchIconImage )
  public void onSearchIconImageClick(View view) {

    // get editable data type, convert to string
    String busNumber = mSearchText.getText().toString();

    if( busNumber.toString().isEmpty() ) {
      Toast.makeText(this, "Please enter valid bus number", Toast.LENGTH_SHORT).show();
    } else {

      try {
        getBusNumber(busNumber); // get Json data and display on list :)
      } catch (NoSuchAlgorithmException error) {
        error.printStackTrace();
      } catch (InvalidKeyException error) {
        error.printStackTrace();
      }
    }
  }

  private void getBusNumber( String busNumber ) throws NoSuchAlgorithmException, InvalidKeyException {

    // create SIGNED URI (PTV specifications)
    Timetable timetable = new Timetable();
    timetable.setBusNumberSearchUri( busNumber );
    String timetableUrl = timetable.buildTTAPIURL();        // get appropriate URL

    // handle network requests
    if( isNetworkAvailable() ) {

      toggleProgressBar();

      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(timetableUrl).build();
      Call call = client.newCall(request);

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

            // toggle Progress Bar
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                toggleProgressBar();
              }
            });

            String jsonData = response.body().string(); // get JSON object

            if (response.isSuccessful()) {

              mSearch = parseSearchDetails(jsonData); // parse JSON object from API :)

              // run on MAIN UI THREAD
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  updateSearchList();
                }
              });

            } else {
              alertUserAboutError(); // dialog box
            }
          } catch (IOException error) {
            Log.e(TAG, "IOException caught: ", error);
          }
          catch (JSONException error) {
            Log.e(TAG, "JSONException caught: ", error);
          }
        }
      });
    } else {
      Toast.makeText( this, R.string.network_unavailable_message, Toast.LENGTH_LONG ).show();
    }

  }

  public Search parseSearchDetails( String jsonData ) throws JSONException {

    Search search = new Search();
    search.setRoutes( getRoute( jsonData ) );

    return search;
  }

  @Nullable
  private Route[] getRoute(String jsonData ) throws JSONException {

    JSONObject searchRootObject = new JSONObject( jsonData );
    JSONArray routesArray = searchRootObject.getJSONArray( "routes" );

    // if there is no objects stored in the array, no return, so display not found image
    if( routesArray.length() == 0 ) {

      // there is nothing in the array
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          //toggle not found message image
          mSearchRecyclerView.setVisibility( View.INVISIBLE );
          mNotFoundPromptImage.setVisibility( View.VISIBLE );
        }
      });

      return null;

    } else {

      //toggle not found message image
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          mSearchRecyclerView.setVisibility( View.VISIBLE );
          mNotFoundPromptImage.setVisibility( View.INVISIBLE );
        }
      });

      Route[] routes = new Route[routesArray.length()]; // create array of objects

      for (int i = 0; i < routesArray.length(); i++) {

        JSONObject routeObj = routesArray.getJSONObject(i);

        Route route = new Route();
        route.setRouteName(routeObj.getString("route_name"));
        route.setRouteNumber(routeObj.getString("route_number"));
        route.setRouteId(routeObj.getInt("route_id"));

        routes[i] = route;
      }

      return routes;
    }
  }

  private void updateSearchList() {
    if( mSearch.getRoutes() != null ) {
      SearchAdapter adapter = new SearchAdapter( this, mSearch.getRoutes() );
      mSearchRecyclerView.setAdapter( adapter );
      RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
      mSearchRecyclerView.setLayoutManager( layoutManager );
      mSearchRecyclerView.setHasFixedSize( true );
    }
  }

  private boolean isNetworkAvailable() {
    
    ConnectivityManager manager = (ConnectivityManager)
            getSystemService(Context.CONNECTIVITY_SERVICE );
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();

    boolean isAvailable = false;
    if( networkInfo != null && networkInfo.isConnected() )
      isAvailable = true;

    return  isAvailable;
  }

  private void alertUserAboutError() {
    AlertDialogFragment dialog = new AlertDialogFragment();
    dialog.show( getFragmentManager(), "error_dialog" );
  }
}
