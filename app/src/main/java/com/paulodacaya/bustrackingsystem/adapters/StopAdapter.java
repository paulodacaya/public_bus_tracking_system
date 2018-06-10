package com.paulodacaya.bustrackingsystem.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.timetable.Stop;
import com.paulodacaya.bustrackingsystem.ui.StopDepartureActivity;
import com.paulodacaya.bustrackingsystem.utilities.Constants;


public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder> {

  private Stop[] mStops;
  private Context mContext;

  public StopAdapter( Context context, Stop[] stops ) {
    mContext = context;
    mStops = stops;
  }

  // gets called when ever a new view holder is needed Views are going to be recycled automatically
  // but in this block of code we create new ones as they are needed
  @Override
  public StopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View view = LayoutInflater.from( parent.getContext() )
            .inflate( R.layout.stop_list_item, parent, false );

    StopViewHolder viewHolder = new StopViewHolder( view, mContext );

    return viewHolder;
  }

  // bridge between the adapter and bind method that was created in HourViewHolder class
  @Override
  public void onBindViewHolder(StopViewHolder holder, int position) {
    holder.bindStop( mStops[position] );
  }

  @Override
  public int getItemCount() {
    return mStops.length;
  }


  public class StopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mStopNameLabel;
    public TextView mDistanceNumberLabel;

    Context mContext;
    public String mStopId;

    public StopViewHolder(View itemView, Context context) {
      super(itemView);

      mContext = context;

      mDistanceNumberLabel = itemView.findViewById( R.id.distanceNumberLabel );
      mStopNameLabel = itemView.findViewById( R.id.stopNameLabel );

      itemView.setOnClickListener( this );
    }

    public void bindStop( Stop stop ) {
      mStopNameLabel.setText( stop.getStopName() );
      mDistanceNumberLabel.setText( stop.getStopDistance() + "" );
      mStopId = stop.getStopId() + ""; // convert integer to string
    }

    @Override
    public void onClick(View v) {

      Intent intent = new Intent( mContext, StopDepartureActivity.class );
      intent.putExtra( Constants.STOP_ID, mStopId );
      intent.putExtra( Constants.STOP_NAME, mStopNameLabel.getText().toString() );
      mContext.startActivity( intent );
    }
  }
}
