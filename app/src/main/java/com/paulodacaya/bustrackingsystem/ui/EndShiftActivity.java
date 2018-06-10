package com.paulodacaya.bustrackingsystem.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EndShiftActivity extends AppCompatActivity {

  @BindView( R.id.endingBusLabel ) TextView mEndingBusLabel;
  @BindView( R.id.onTimePercentageText ) TextView mOnTimePercentageText;
  @BindView( R.id.returnHomeButton ) Button mReturnHomeButton;
  @BindView( R.id.changeDirectionButton ) Button mChangeDirectionButton;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_end_shift);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    // get Intent values
    Intent intent = getIntent();
    String busNumber = intent.getStringExtra( Constants.BUS_NUMBER );
    int totalOnTimeTaps = intent.getIntExtra( Constants.TOTAL_ON_TIME_TAPS, 0 );
    int totalTaps = intent.getIntExtra( Constants.TOTAL_TAPS, 1 ); // cannot divide by zero
    //----------------------------------------------------------------------------------------------

    float onTimePercentage = ( (float) totalOnTimeTaps/totalTaps )*100;

    mEndingBusLabel.setText( "Ending Bus " + busNumber + " Session" );
    mOnTimePercentageText.setText( Constants.round(onTimePercentage, 2) + "% on time\nin this session" );
  }

  @OnClick ( R.id.changeDirectionButton )
  public void returnToRouteDirectionActivity() {
    finish(); // The last activity on the stack should be routeDirectionActivity
  }

  @OnClick ( R.id.returnHomeButton )
  public void returnToDriverNavigationActivity() {
    startActivity( new Intent( EndShiftActivity.this, DriverNavigationActivity.class ) );
    finish(); // remove activity from stack
  }
}
