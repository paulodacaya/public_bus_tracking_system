package com.paulodacaya.bustrackingsystem.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.adapters.BusRouteAdapter;
import com.paulodacaya.bustrackingsystem.database.DatabaseHandler;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

  @BindView( R.id.toolbar ) Toolbar mToolbar;
  @BindView( R.id.drawer_layout ) DrawerLayout mDrawerLayout;
  @BindView( R.id.nav_view ) NavigationView mNavigationView;
  @BindView( R.id.contentTitle ) TextView mContentTitle;
  @BindView( R.id.subContentTitle ) TextView mSubContentTitle;
  @BindView( R.id.noBusRoutesPrompt ) ImageView mNoBusRoutesPromptImage;
  @BindView( R.id.adminHomeBody ) ConstraintLayout mAdminHomeBody;
  @BindView( R.id.adminAnalysisBody ) ConstraintLayout mAdminAnalysisBody;

  @BindView( R.id.busRouteRecyclerView ) RecyclerView mBusRouteRecyclerView;

  DatabaseHandler mDatabaseHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin_navigation);
    ButterKnife.bind( this );

    setSupportActionBar(mToolbar);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    mNavigationView.setNavigationItemSelectedListener(this);

    // ActionBar
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    actionBar.setIcon( R.drawable.pbt_logo_letters_white_small );
    actionBar.setTitle( "" );
  
    // Toggle content to home view
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
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_home) {
      toggleContent( id );

    } else if (id == R.id.nav_analysis) {
      toggleContent( id );

      // If database exists
      if( isDatabaseExist() ) {
       
        mBusRouteRecyclerView.setVisibility( View.VISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.INVISIBLE );

        mDatabaseHandler = new DatabaseHandler(this );  // instantiate databaseHandler
        displayBusRouteList();
      
      } else {
        
        // Display "No data prompt"
        mBusRouteRecyclerView.setVisibility( View.INVISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.VISIBLE );
      }

    } else if (id == R.id.nav_exit) {
      startActivity( new Intent( AdminNavigationActivity.this, AuthenticationActivity.class ));
      finish();
    }
    
    mDrawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  private void toggleContent( int id ) {

    switch( id ) {
      case R.id.nav_home:
        mContentTitle.setText( R.string.content_title_home_page );
        mSubContentTitle.setText( R.string.content_sub_title_home_page );
        
        mAdminHomeBody.setVisibility( View.VISIBLE );
        mAdminAnalysisBody.setVisibility( View.INVISIBLE );
        break;
  
      case R.id.nav_analysis:
        mContentTitle.setText( R.string.content_title_analysis );
        mSubContentTitle.setText( R.string.content_sub_title_analysis );
  
        mAdminHomeBody.setVisibility( View.INVISIBLE );
        mAdminAnalysisBody.setVisibility( View.VISIBLE );
        break;
    }
  }

  private void displayBusRouteList() {

    List<String> busRouteList = mDatabaseHandler.getAllBusRoute();
    
    BusRouteAdapter adapter = new BusRouteAdapter( this, busRouteList );
    mBusRouteRecyclerView.setAdapter( adapter );
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
    mBusRouteRecyclerView.setLayoutManager( layoutManager );
    mBusRouteRecyclerView.setHasFixedSize( true );
  }

  public boolean isDatabaseExist() {

    /***************************************************************************************
     *    Title: (STACK OVERFLOW) How do I check whether a SQLite database file exists using Java?
     *    Author: Heisenbug
     *    Date: answered Aug 17 '11 at 23:09
     *    Code version: --
     *    Availability: --
     ***************************************************************************************/
    File databasePath = this.getDatabasePath( Constants.DB_NAME );

    if ( databasePath.exists() )
      return true;
    else
      return false;
  }
}
