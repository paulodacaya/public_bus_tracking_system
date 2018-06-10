package com.paulodacaya.bustrackingsystem.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.paulodacaya.bustrackingsystem.ui.AdminNavigationActivity;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

  private SQLiteDatabase mDB;
  private Context mContext;
  private final String TABLE_BUS_NUMBER;

  public DatabaseHandler( Context context ) {
    super( context, Constants.DB_NAME, null, Constants.DB_VERSION);
    mContext = context;
    TABLE_BUS_NUMBER = null;
  }

  public DatabaseHandler(Context context, String tableBusNumber ) {
    super( context, Constants.DB_NAME, null, Constants.DB_VERSION );
    mContext = context;
    TABLE_BUS_NUMBER = tableBusNumber;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    // onCreate() is only run when the database file did not exist and was just created.
    // If onCreate() returns successfully (doesn't throw an exception), the database is assumed
    // to be created with the requested version number.

    final String CREATE_BUS_NUMBER_TABLE = "CREATE TABLE " + TABLE_BUS_NUMBER + " ("
            + Constants.KEY_ID + " INTEGER PRIMARY KEY, "
            + Constants.KEY_DATE + " TEXT, "
            + Constants.KEY_STOP_NAME + " TEXT, "
            + Constants.KEY_SCHEDULED_TIME + " TEXT, "
            + Constants.KEY_ARRIVAL_TIME + " TEXT, "
            + Constants.KEY_DELTA + " LONG "
            + ")";

    db.execSQL( CREATE_BUS_NUMBER_TABLE ); // execute SQL command
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    // onUpgrade() is only called when the database file exists but the stored version number is
    // lower than requested in constructor. The onUpgrade() should update the table schema to the
    // requested version.

    final String DB_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_BUS_NUMBER;

    db.execSQL( DB_DROP_TABLE );
    onCreate( db );
  }

  public void openWritableAccess() {
    mDB = this.getWritableDatabase(); // open database for writable access
  }

  public void closeWritableAccess() {
    mDB.close();
  }

  public boolean isTableNameExist( String tableName ) {

    /***************************************************************************************
     *    Title: (STACK OVERFLOW) How does one check if a table exists in an Android SQLite database?
     *    Author: Nikolay DS
     *    Date: answered Oct 22 '11 at 23:52
     *    Code version: --
     *    Availability: --
     ***************************************************************************************/

    if( mDB == null || !mDB.isOpen() ) {
      mDB = getReadableDatabase();
    }

    if( !mDB.isReadOnly() ) {
      mDB.close();
      mDB = getReadableDatabase();
    }

    Cursor cursor = mDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
    if( cursor != null ) {
      if( cursor.getCount() > 0 ) {
        mDB.close();
        cursor.close();
        return true;
      }
      mDB.close();
      cursor.close();
    }

    return false;
  }

  public void createTable( String tableName ) {

    mDB = getWritableDatabase();

    final String CREATE_BUS_NUMBER_TABLE = "CREATE TABLE " + tableName + " ("
            + Constants.KEY_ID + " INTEGER PRIMARY KEY, "
            + Constants.KEY_DATE + " TEXT, "
            + Constants.KEY_STOP_NAME + " TEXT, "
            + Constants.KEY_SCHEDULED_TIME + " TEXT, "
            + Constants.KEY_ARRIVAL_TIME + " TEXT, "
            + Constants.KEY_DELTA + " LONG "
            + ")";

    mDB.execSQL( CREATE_BUS_NUMBER_TABLE ); // execute SQL command

    mDB.close(); // close database
  }

  /**
   * ISUD OPERATIONS - insert, select, update and delete
   *
   * NOTES:
   *  -> writing data during shift map interface, so we can use lifecycle methods to open write access.
   *  -> reading data during admin section, handle reading access in methods.
   */

  public void addBusStopData( StopData stopData ) {

    // hash-map object that allows key value pairs that can be added to database
    ContentValues contentValues = new ContentValues();
    contentValues.put( Constants.KEY_DATE, stopData.getDate() );
    contentValues.put( Constants.KEY_STOP_NAME, stopData.getStopName() );
    contentValues.put( Constants.KEY_SCHEDULED_TIME, stopData.getScheduledTime() );
    contentValues.put( Constants.KEY_ARRIVAL_TIME, stopData.getArrivalTime() );
    contentValues.put( Constants.KEY_DELTA, stopData.getDelta() );

    // Insert the row into Database
    mDB.insert( TABLE_BUS_NUMBER, null, contentValues );

  }

  public List<String> getAllBusRoute() {

    mDB = getReadableDatabase();
    List<String> tableNameList = new ArrayList<>();

    /***************************************************************************************
     *    Title: (STACK OVERFLOW) How to get all table names in android sqlite database?
     *    Author: DawnYu
     *    Date: answered Sep 9 '15 at 3:12
     *    Code version: --
     *    Availability: --
     ***************************************************************************************/
    // get all table names in database
    Cursor cursor = mDB.rawQuery( "SELECT name FROM sqlite_master WHERE type='table' " +
            "AND name!='android_metadata' order by name", null );

    // check if there is something in cursor first
    if( cursor.moveToFirst() ) {
      do {
        tableNameList.add( cursor.getString( cursor.getColumnIndex( Constants.KEY_TABLE_NAME ))); // add to List
      } while( cursor.moveToNext() );
    }

    cursor.close(); // close cursor
    mDB.close();    // close database connection

    return tableNameList;
  }

  public List<StopData> getAllDataOfBusRoute( String busRoute ) {

    mDB = getReadableDatabase(); // get readable access from database
    List<StopData> stopDataList = new ArrayList<>();

    Cursor cursor = mDB.query(
            busRoute,
            new String[] { // get entire row data
                    Constants.KEY_ID, Constants.KEY_DATE, Constants.KEY_STOP_NAME,
                    Constants.KEY_SCHEDULED_TIME, Constants.KEY_ARRIVAL_TIME, Constants.KEY_DELTA
            },
            null,
            null,
            null,
            null,
            null
    );


    // loop through cursor and assign to List
    // first check if there is something in the cursor
    if( cursor.moveToFirst() ) {

      do {

        StopData stopData = new StopData();

        stopData.setId( cursor.getInt( cursor.getColumnIndex( Constants.KEY_ID )));
        stopData.setDate( cursor.getString( cursor.getColumnIndex( Constants.KEY_DATE )));
        stopData.setStopName( cursor.getString( cursor.getColumnIndex( Constants.KEY_STOP_NAME )));
        stopData.setScheduledTime( cursor.getString( cursor.getColumnIndex( Constants.KEY_SCHEDULED_TIME )));
        stopData.setArrivalTime( cursor.getString( cursor.getColumnIndex( Constants.KEY_ARRIVAL_TIME )));

        // covert milliseconds to minutes for
        long deltaMillis = cursor.getLong( cursor.getColumnIndex( Constants.KEY_DELTA ));
        int deltaSeconds = Math.abs( (int) ( ( deltaMillis/1000)%60 ) );
        long deltaMinutes = ( (deltaMillis / 1000)-deltaSeconds )/60;

        // convert to float value
        String minSecString = deltaMinutes + "." + deltaSeconds;
        float minSecsFloat = (float) Double.parseDouble( minSecString );

        stopData.setDeltaMinSec( minSecsFloat );

        // add to ArrayList
        stopDataList.add( stopData );

        // repeat for all elements in the table
      } while( cursor.moveToNext() ); // keeps going until null row
    }

    cursor.close(); // close cursor
    mDB.close();    // close database connection

    return stopDataList;
  }

  public StopData getStopData( String busRoute, String id ) {

    mDB = getReadableDatabase();

    // get specific row with the id passed through
    Cursor cursor = mDB.query(
            busRoute,
            new String[] { // get entire row data
                    Constants.KEY_ID, Constants.KEY_DATE, Constants.KEY_STOP_NAME,
                    Constants.KEY_SCHEDULED_TIME, Constants.KEY_ARRIVAL_TIME, Constants.KEY_DELTA
            },
            Constants.KEY_ID + " = ?",
            new String[] { id },
            null,
            null,
            null,
            null
    );

    // first check if query was successful, if not, return null
    if( cursor != null )
      cursor.moveToFirst();
    else
      return null;

    StopData stopData = new StopData(); // create stop data to return

    stopData.setId( cursor.getInt( cursor.getColumnIndex( Constants.KEY_ID )));
    stopData.setDate( cursor.getString( cursor.getColumnIndex( Constants.KEY_DATE )));
    stopData.setStopName( cursor.getString( cursor.getColumnIndex( Constants.KEY_STOP_NAME )));
    stopData.setScheduledTime( cursor.getString( cursor.getColumnIndex( Constants.KEY_SCHEDULED_TIME )));
    stopData.setArrivalTime( cursor.getString( cursor.getColumnIndex( Constants.KEY_ARRIVAL_TIME )));
    stopData.setDelta( cursor.getLong( cursor.getColumnIndex( Constants.KEY_DELTA )) );

    mDB.close();
    cursor.close();

    return stopData; // return object with stored information about selected row
  }

  public int getOnTimeStopDataCount( String busRoute ) {

    mDB = getReadableDatabase();
    int totalOnTimeStopData;

    Cursor cursor = mDB.query(
            busRoute,
            new String[] { Constants.KEY_DELTA },
            Constants.KEY_DELTA + " >= -300000 AND "+ Constants.KEY_DELTA  +" <= 300000",
            null,
            null,
            null,
            null
    );

    totalOnTimeStopData = cursor.getCount();

    mDB.close();
    cursor.close();

    return totalOnTimeStopData;
  }

  public int getTotalStopDataCount( String busRoute ) {

    mDB = getReadableDatabase();

    int totalStopData;
    String countQuery = "SELECT * FROM " + busRoute;
    Cursor cursor = mDB.rawQuery( countQuery, null );
    totalStopData = cursor.getCount();

    mDB.close();
    cursor.close();

    return totalStopData;
  }

  public void deleteStopData( String busRoute, String id ) {

    mDB = getWritableDatabase(); // open writable access

    mDB.delete(
            busRoute,
            Constants.KEY_ID + " = ?",
            new String[] { id }
    );


    mDB.close(); // close writable access
  }


}
