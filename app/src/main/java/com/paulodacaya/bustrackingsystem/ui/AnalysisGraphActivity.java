package com.paulodacaya.bustrackingsystem.ui;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.database.DatabaseHandler;
import com.paulodacaya.bustrackingsystem.database.StopData;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AnalysisGraphActivity extends AppCompatActivity implements OnChartValueSelectedListener {

  @BindView( R.id.dataScrollView ) ScrollView mDataScrollView;
  @BindView( R.id.busRouteAnalysisLabel ) TextView mBusRouteAnalysisLabel;
  @BindView( R.id.lineChart ) LineChart mLineChart;
  @BindView( R.id.pieChart ) PieChart mPieChart;
  @BindView( R.id.examineButton ) Button mExamineButton;
  @BindView( R.id.noDataOnRoutePrompt ) ImageView mNoDataOnRoutePrompt;

  DatabaseHandler mDatabaseHandler;
  List<StopData> mStopDataList;
  LineData mLineData;
  PieData mPieData;

  private String mSelectedDataPointId;
  private String mBusRoute;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_analysis_graph);
    ButterKnife.bind( this );

    //----------------------------------------------------------------------------------------------
    // get intent string values
    Intent intent = getIntent();
    mBusRoute = intent.getStringExtra( Constants.BUS_ROUTE );
    //----------------------------------------------------------------------------------------------

    // Initial examine button state
    mExamineButton.setVisibility( View.INVISIBLE );

    // set title text of activity
    String busRouteAnalysisText = mBusRoute.replace( "_", " " ) + " Analytical Graphs";
    mBusRouteAnalysisLabel.setText( busRouteAnalysisText );

    mDatabaseHandler = new DatabaseHandler( this ); // instantiate database handler
    mLineChart.setOnChartValueSelectedListener( this );     // register click listener on chart

    displayLineChart(); // display line Chart Data
    displayPieChart();  // display Pie Chart Data
  }

  private void displayLineChart() {

    // get data corresponding to table name
    mStopDataList = mDatabaseHandler.getAllDataOfBusRoute( mBusRoute );

    // invalid for 0 and 1 data point
    if( mStopDataList.size() < 2 ) {

      mDataScrollView.setVisibility( View.INVISIBLE );
      mNoDataOnRoutePrompt.setVisibility( View.VISIBLE );
    } else {

      mDataScrollView.setVisibility( View.VISIBLE );
      mNoDataOnRoutePrompt.setVisibility( View.INVISIBLE );

      // GENERAL Line Chart settings
      Description description = new Description();
      description.setText( getString(R.string.line_chart_description_label) );
      description.setTextColor( ResourcesCompat.getColor(getResources(), R.color.colorDefaultText, null) );
      description.setTextSize( Constants.TEXT_SIZE_LARGE );
      description.setPosition( 1200f,  200f );
      mLineChart.setDescription( description );


      //----------------------------------------------------------------------------------------------
      // GET X-AXIS and set settings
      XAxis xAxis = mLineChart.getXAxis();
      xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );
      xAxis.setTextSize( Constants.TEXT_SIZE );
      xAxis.setTextColor( ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null) );
      xAxis.setDrawAxisLine( true );
      xAxis.setDrawGridLines( false );

      // create custom X-Axis with string values in relation to index
      int index = 0;
      String[] scheduledTimes = new String[ mStopDataList.size() ];
      for( StopData stopData : mStopDataList ) {
        scheduledTimes[index] = stopData.getDate();
        index++;
      }

      xAxis.setValueFormatter( new MyXAxisValueFormatter(scheduledTimes) );
      xAxis.setLabelRotationAngle( 45f );
      xAxis.setGranularityEnabled( true );
      xAxis.setGranularity( 1f );


      //----------------------------------------------------------------------------------------------
      // GET Y-AXIS (left and right) and set settings
      YAxis leftAxis = mLineChart.getAxisLeft();
      YAxis rightAxis = mLineChart.getAxisRight();
      rightAxis.setDrawAxisLine( false );
      rightAxis.setDrawGridLines( false );
      rightAxis.setDrawLabels( false );
      leftAxis.setDrawGridLines( false );
      leftAxis.setAxisMaximum( 30f );
      leftAxis.setAxisMinimum( -30f );
      leftAxis.setTextSize( Constants.TEXT_SIZE );
      leftAxis.setTextColor( ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null) );

      leftAxis.setDrawZeroLine( true ); // draw line at zero
      leftAxis.setZeroLineWidth( 1.5f );
      leftAxis.setZeroLineColor( ResourcesCompat.getColor(getResources(), R.color.colorSecondaryAccent, null) );

      leftAxis.addLimitLine( getLimitLine( 5 ));
      leftAxis.addLimitLine( getLimitLine( -5 ));

      //----------------------------------------------------------------------------------------------
      // GET LEGEND and set settings
      Legend legend = mLineChart.getLegend();
      legend.setTextSize( Constants.TEXT_SIZE_LARGE );
      legend.setTextColor( Color.BLACK );
      legend.setPosition( Legend.LegendPosition.ABOVE_CHART_RIGHT );
      legend.setForm( Legend.LegendForm.LINE );
      legend.setFormSize( 20f );



      //----------------------------------------------------------------------------------------------
      // add data to List of Entry Objects
      List<Entry> entries = new ArrayList<>();
      for( StopData stopdata : mStopDataList ) {
        entries.add( new Entry( stopdata.getId(), stopdata.getDeltaMinSec() ));
      }


      LineDataSet lineDataSet1 = new LineDataSet( entries ,
              mBusRoute.replace( "_", " ") + " DELTA Values" );
      lineDataSet1.setColor( ResourcesCompat.getColor(getResources(), R.color.colorDefaultText, null) );
      lineDataSet1.setValueTextColor( Color.BLACK );
      lineDataSet1.setValueTextSize( Constants.TEXT_SIZE );
      lineDataSet1.setLineWidth( 3f );
      lineDataSet1.setCircleColor( ResourcesCompat.getColor(getResources(), R.color.colorDefaultText, null) );
      lineDataSet1.setCircleColorHole( ResourcesCompat.getColor(getResources(), R.color.colorAccentLight, null) );
      lineDataSet1.setCircleRadius( 5f );
      lineDataSet1.setCircleHoleRadius( 2.5f );
      lineDataSet1.setHighLightColor( Color.RED );
      lineDataSet1.setHighlightLineWidth( 1.2f );

      //----------------------------------------------------------------------------------------------
      // Usually used to add more that one line on graph (CAN BE USED IN FUTURE)
      List<ILineDataSet> dataSets = new ArrayList<>();
      dataSets.add(lineDataSet1);


      mLineData = new LineData( dataSets );
      mLineChart.setData( mLineData );
      mLineChart.invalidate(); // refresh
    }
  }

  private void displayPieChart() {

    //----------------------------------------------------------------------------------------------
    // GENERAL pie chart settings
    mPieChart.setUsePercentValues( true );
    Description description = new Description();
    description.setText( getString(R.string.pie_chart_description_label) );
    description.setTextColor( ResourcesCompat.getColor(getResources(), R.color.colorDefaultText, null) );
    description.setTextSize( Constants.TEXT_SIZE_LARGE );
    description.setPosition( 1190f,  200f );
    mPieChart.setDescription( description );
    mPieChart.setExtraOffsets( 5 , 10, 5, 5 );
    mPieChart.setDragDecelerationFrictionCoef( 0.95f );
    mPieChart.setDrawHoleEnabled( true );
    mPieChart.setHoleColor( ResourcesCompat.getColor(getResources(), R.color.colorAccentLight, null) );
    mPieChart.setTransparentCircleRadius( 61f ); // 3rd effect

    //----------------------------------------------------------------------------------------------
    // GET LEGEND and set settings
    Legend legend = mPieChart.getLegend();
    legend.setTextSize( Constants.TEXT_SIZE_LARGE );
    legend.setTextColor( Color.BLACK );
    legend.setPosition( Legend.LegendPosition.BELOW_CHART_CENTER );
    legend.setForm( Legend.LegendForm.CIRCLE );
    legend.setFormSize( 20f );
    legend.setXEntrySpace( 20f );

    //----------------------------------------------------------------------------------------------
    // Get and Add data
    int totalStopData = mDatabaseHandler.getTotalStopDataCount( mBusRoute );
    int totalOnTimeStopData = mDatabaseHandler.getOnTimeStopDataCount( mBusRoute );
    int totalNotOnTimeStopData = totalStopData - totalOnTimeStopData;

    // add data to Pie Entry
    List<PieEntry> entries = new ArrayList<>();
    entries.add( new PieEntry( totalOnTimeStopData, "On-Time" ));
    entries.add( new PieEntry( totalNotOnTimeStopData, "Not On-Time"));

    PieDataSet set = new PieDataSet(entries, "");
    set.setSliceSpace( 5f ); // spacing between each statistic
    set.setSelectionShift( 5f );
    set.setColors( ColorTemplate.MATERIAL_COLORS );

    mPieData = new PieData(set);
    mPieData.setValueTextSize( 20f );
    mPieData.setValueTextColor( ResourcesCompat.getColor(getResources(), R.color.colorDefaultText, null) );


    mPieChart.setData(mPieData);
    mPieChart.invalidate(); // refresh
  }

  @NonNull
  private LimitLine getLimitLine( float value ) {
    LimitLine ll = new LimitLine( value ); // set where the line should be drawn
    ll.setLineColor( ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null) );
    ll.setLineWidth( 2f );
    ll.setTextSize( Constants.TEXT_SIZE );
    ll.setLabel("on-time boundary");
    return ll;
  }

  @Override
  public void onValueSelected(Entry e, Highlight h) {
    /**
     * Called when a value has been selected inside the chart.
     * @param e The selected Entry.
     * @param h The corresponding highlight object that contains information about the highlighted position
     */

    mExamineButton.setVisibility( View.VISIBLE );
    mSelectedDataPointId = String.valueOf( (int) e.getX() ); // convert float to int then to string (easier for database)
  }

  @Override
  public void onNothingSelected() {
    // called when deselected or tapped outside of graph
    mExamineButton.setVisibility( View.INVISIBLE );
  }

  @OnClick (R.id.examineButton )
  public void startExamineActivity() {
    Intent intent = new Intent( AnalysisGraphActivity.this, ExamineActivity.class );
    intent.putExtra( Constants.ROW_ID, mSelectedDataPointId );
    intent.putExtra( Constants.BUS_ROUTE, mBusRoute );
    startActivity( intent );
    finish();
  }

  // CUSTOM X-AXIS FORMATTER
  public class MyXAxisValueFormatter implements IAxisValueFormatter {

    private String[] mValues;

    public MyXAxisValueFormatter(String[] values) {
      mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
      return mValues[ (int) (value-1) ];
    }
  }
}
