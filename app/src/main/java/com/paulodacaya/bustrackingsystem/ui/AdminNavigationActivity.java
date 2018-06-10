package com.paulodacaya.bustrackingsystem.ui;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

  @BindView( R.id.toolbar ) Toolbar mToolbar;
  @BindView( R.id.drawer_layout ) DrawerLayout mDrawerLayout;
  @BindView( R.id.nav_view ) NavigationView mNavigationView;
  @BindView( R.id.contentTitle ) TextView mContentTitle;
  @BindView( R.id.subContentTitle ) TextView mSubContentTitle;
  @BindView( R.id.homPageContentTitle1 ) TextView mHomePageContentTitle1;
  @BindView( R.id.homePageContentBody1 ) TextView mHomePageContentBody1;
  @BindView( R.id.infoPromptImage ) ImageView mInfoPromptImage;
  @BindView( R.id.noBusRoutesPrompt ) ImageView mNoBusRoutesPromptImage;

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

    //------------------------------------------------------------------------------------------
    // TODO: FIX PADDING ISSUE WITH PBT LOGO
    android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    actionBar.setIcon( R.drawable.pbt_logo_letters_white_small );
    actionBar.setTitle( "" );
    //------------------------------------------------------------------------------------------

    toggleContent( R.id.nav_home ); // toggle content to home view

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
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.admin_navigation, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_home) {
      toggleContent( id );

    } else if (id == R.id.nav_analysis) {
      toggleContent( id );

      // first check if database exists
      if( isDatabaseExist() ) {
        mBusRouteRecyclerView.setVisibility( View.VISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.INVISIBLE );

        mDatabaseHandler = new DatabaseHandler(this );  // instantiate databaseHandler
        displayBusRouteList();
      } else {
        mBusRouteRecyclerView.setVisibility( View.INVISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.VISIBLE );
      }

    } else if (id == R.id.nav_account) {
      toggleContent( id );

    } else if (id == R.id.nav_exit) {
      startActivity( new Intent( AdminNavigationActivity.this, AuthenticationActivity.class ));
      finish(); // remove activity from stack
    }

    mDrawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  private void toggleContent( int id ) {

    switch( id ) {
      case R.id.nav_home:
        mContentTitle.setText( R.string.content_title_home_page );
        mSubContentTitle.setText( R.string.content_sub_title_home_page );
        mHomePageContentTitle1.setText( R.string.analysis_content_title_1 );
        mHomePageContentBody1.setText( R.string.analysis_content_body_1 );

        mInfoPromptImage.setVisibility( View.VISIBLE );
        mHomePageContentTitle1.setVisibility( View.VISIBLE );
        mHomePageContentBody1.setVisibility( View.VISIBLE );

        mBusRouteRecyclerView.setVisibility( View.INVISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.INVISIBLE);
        break;

      case R.id.nav_analysis:
        mContentTitle.setText( R.string.content_title_analysis );
        mSubContentTitle.setText( R.string.content_sub_title_analysis );

        mBusRouteRecyclerView.setVisibility( View.VISIBLE );

        mInfoPromptImage.setVisibility( View.INVISIBLE );
        mHomePageContentTitle1.setVisibility( View.INVISIBLE );
        mHomePageContentBody1.setVisibility( View.INVISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.INVISIBLE);
        break;

      case R.id.nav_account:
        mContentTitle.setText( R.string.content_title_my_account );
        mSubContentTitle.setText( R.string.content_sub_title_my_account );

        mInfoPromptImage.setVisibility( View.INVISIBLE );
        mHomePageContentTitle1.setVisibility( View.INVISIBLE );
        mHomePageContentBody1.setVisibility( View.INVISIBLE );
        mBusRouteRecyclerView.setVisibility( View.INVISIBLE );
        mNoBusRoutesPromptImage.setVisibility( View.INVISIBLE);
        break;
    }
  }

  private void displayBusRouteList() {

    List<String> busRouteList = mDatabaseHandler.getAllBusRoute();

    // show list
    BusRouteAdapter adapter = new BusRouteAdapter( this, busRouteList );
    mBusRouteRecyclerView.setAdapter( adapter );
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
    mBusRouteRecyclerView.setLayoutManager( layoutManager );
    mBusRouteRecyclerView.setHasFixedSize( true ); // recommended to increase performance
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
