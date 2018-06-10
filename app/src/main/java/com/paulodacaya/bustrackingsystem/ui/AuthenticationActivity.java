package com.paulodacaya.bustrackingsystem.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.database.DatabaseHandler;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthenticationActivity extends AppCompatActivity {

  @BindView( R.id.publicButton ) Button mPublicRadioButton;
  @BindView( R.id.busDriverButton) Button mDriverRadioButton;
  @BindView( R.id.adminButton ) Button mAdminRadioButton;
  @BindView( R.id.logInButton ) Button mLogInButton;

  private String mPublicText;
  private String mDriverText;
  private String mAdminText;
  private String selected;

  public AuthenticationActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_authentication);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    mPublicText = mPublicRadioButton.getText().toString();
    mDriverText = mDriverRadioButton.getText().toString();
    mAdminText = mAdminRadioButton.getText().toString();

    // initialise default button state
    mPublicRadioButton.setBackgroundResource( R.drawable.bg_rounded_orange );
    mPublicRadioButton.setTextColor( Color.parseColor( "#ffffff" ) );
    selected = mPublicText;

    /** CAREFUL: RUN THIS COMMAND TO DELETE DATABASE */
    // this.deleteDatabase(Constants.DB_NAME);
  }

  // Handle radio button clicks
  public void onRadioButtonClick(View view) {
    switch (view.getId()) {
      case R.id.publicButton:
        changeRadioButtons(mPublicRadioButton, mDriverRadioButton, mAdminRadioButton);
        break;

      case R.id.busDriverButton:
        changeRadioButtons(mDriverRadioButton, mPublicRadioButton, mAdminRadioButton);
        break;

      case R.id.adminButton:
        changeRadioButtons(mAdminRadioButton, mDriverRadioButton, mPublicRadioButton);
        break;
    }
  }

  public void changeRadioButtons(Button selectedButton, Button otherButton1, Button otherButton2 ) {
    selectedButton.setBackgroundResource( R.drawable.bg_rounded_orange );
    selectedButton.setTextColor( Color.parseColor( "#ffffff" ) );

    otherButton1.setBackgroundResource( R.drawable.bg_roundedstroke_black );
    otherButton1.setTextColor( Color.parseColor("#111111" ));

    otherButton2.setBackgroundResource( R.drawable.bg_roundedstroke_black );
    otherButton2.setTextColor( Color.parseColor("#111111" ));

    selected = selectedButton.getText().toString();
  }

  @OnClick ( R.id.logInButton )
  public void startHomeActivity() {
    if( selected.equals(mPublicText) ) {
      startActivity( new Intent(this, UserNavigationActivity.class) );
    } else if( selected.equals(mDriverText) ) {
      startActivity( new Intent(this, DriverNavigationActivity.class) );
    } else if( selected.equals(mAdminText) ) {
      startActivity( new Intent(this, AdminNavigationActivity.class) );
    }
    finish();
  }
}
