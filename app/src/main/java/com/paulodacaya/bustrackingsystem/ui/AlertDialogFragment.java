package com.paulodacaya.bustrackingsystem.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.paulodacaya.bustrackingsystem.R;

public class AlertDialogFragment extends DialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    Context context = getActivity(); // get the activity when this is instantiated

    // Builder is a nested class inside AlertDialog class
    // configure alert dialog and return it ( chaining the methods to configure )
    AlertDialog.Builder builder = new AlertDialog.Builder( context )
            .setTitle( R.string.error_title )
            .setMessage( R.string.error_message )
            .setPositiveButton( R.string.error_ok_button_text, null );

    // create dialog with the builder method of create
    AlertDialog dialog = builder.create();

    return dialog;
  }
}