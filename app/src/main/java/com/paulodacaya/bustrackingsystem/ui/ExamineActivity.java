package com.paulodacaya.bustrackingsystem.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.database.DatabaseHandler;
import com.paulodacaya.bustrackingsystem.database.StopData;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExamineActivity extends AppCompatActivity {

  @BindView( R.id.dateText ) TextView mDateText;
  @BindView( R.id.stopText ) TextView mStopText;
  @BindView( R.id.scheduledTimeText ) TextView mScheduledTimeText;
  @BindView( R.id.arrivalTimeText ) TextView mArrivalTimeText;
  @BindView( R.id.timeDifferenceText ) TextView mTimeDifferentText;
  @BindView( R.id.deleteDataPointButton ) Button mDeleteDataPointButton;

  private AlertDialog.Builder mAlertDialogBuilder;
  private AlertDialog mDialog;
  private LayoutInflater mInflater;

  private DatabaseHandler mDatabaseHandler;
  private String mStopDataId;
  private String mBusRoute;
  private StopData mStopData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examine_activty);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    // get Intent Values
    Intent intent = getIntent();
    mStopDataId = intent.getStringExtra( Constants.ROW_ID );
    mBusRoute = intent.getStringExtra( Constants.BUS_ROUTE );
    //----------------------------------------------------------------------------------------------

    mDatabaseHandler = new DatabaseHandler( this );             // instantiate Database handler
    mStopData = mDatabaseHandler.getStopData( mBusRoute, mStopDataId ); // get stop data corresponding to table and row id

    // set text
    mDateText.setText( mStopData.getDate() );
    mStopText.setText( mStopData.getStopName() );
    mScheduledTimeText.setText( mStopData.getScheduledTime() );
    mArrivalTimeText.setText( mStopData.getArrivalTime() );

    // refactor delta time
    long deltaMillis = mStopData.getDelta();

    String lateEarly = "PERFECTLY ON TIME";
    if( deltaMillis < 0 )
      lateEarly = "EARLY";
    else if( deltaMillis > 0 )
      lateEarly = "LATE";

    int deltaSeconds = Math.abs( (int) ( ( deltaMillis/1000)%60 ) );
    long deltaMinutes = Math.abs( ( (deltaMillis / 1000)-deltaSeconds )/60 );

    String timeDifferenceText = deltaMinutes + "m " + deltaSeconds + "s " + lateEarly;
    mTimeDifferentText.setText( timeDifferenceText ); // set delta time
  }
  
  /**
   * ISSUE:
   * When deleting data point, it removes it from the database table. However,
   * the X-axis custom formatter is dependent on the ascending order of data points from 1, 2, 3...
   * to successfully display x-axis as date values e.g. 2018-05-11.
   * Deleting a data point will obscure the ascending data and result in a NullPointerException.
   *
   * POSSIBLE SOLUTIONS but have not been implemented:
   * * Completely remove X-axis formatter and just display single digit values, though it
   *   doesn't read as expected
   *
   * * Create a new table, copy values over with correct ascending values and delete previous table.
   * */
  @OnClick ( R.id.deleteDataPointButton )
  public void deleteDataPoint() {

    // create a alert dialog to make sure user wants to delete data point
    mAlertDialogBuilder = new AlertDialog.Builder( this );
    mInflater = LayoutInflater.from( this );

    View view = mInflater.inflate( R.layout.confirmation_dialog, null );

    Button sureCancelButton = view.findViewById( R.id.sureCancelButton );
    Button sureDeleteButton = view.findViewById( R.id.sureDeleteButton );

    // create alertDialog
    mAlertDialogBuilder.setView( view );
    mDialog = mAlertDialogBuilder.create();
    mDialog.show(); // show  dialog

    /** Handle Cancel button click */
    sureCancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mDialog.dismiss();
      }
    });

    /** Handle Delete button click */
    sureDeleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        mDatabaseHandler.deleteStopData( mBusRoute, mStopDataId ); // delete data point

        mDialog.dismiss();

        Intent intent = new Intent( ExamineActivity.this, AdminNavigationActivity.class );
        intent.putExtra( Constants.BUS_ROUTE, mBusRoute );
        finish(); // remove activity from stack, return to previous activity
      }
    });

  }
}
