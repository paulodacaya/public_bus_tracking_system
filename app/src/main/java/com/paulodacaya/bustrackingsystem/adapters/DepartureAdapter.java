package com.paulodacaya.bustrackingsystem.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.timetable.Departure;

public class DepartureAdapter extends RecyclerView.Adapter<DepartureAdapter.DepartureViewHolder> {

  private Departure[] mDepartures;
  private Context mContext;

  public DepartureAdapter(Context context, Departure[] departures ) {
    mContext = context;
    mDepartures = departures;
  }

  @NonNull
  @Override
  public DepartureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View view = LayoutInflater.from( parent.getContext() )
            .inflate( R.layout.departure_list_item, null );

    DepartureViewHolder viewHolder = new DepartureViewHolder( view, mContext );

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DepartureViewHolder holder, int position) {
    holder.bindDirection( mDepartures[position] );
  }

  @Override
  public int getItemCount() {
    return mDepartures.length;
  }



  public class DepartureViewHolder extends RecyclerView.ViewHolder {

    public TextView mScheduledTimeLabel;
    public Context mContext;

    public DepartureViewHolder(View itemView, Context context) {
      super(itemView);
      mContext = context;

      mScheduledTimeLabel = itemView.findViewById( R.id.scheduledTimeLabel );
    }

    public void bindDirection( Departure departure ) {

      mScheduledTimeLabel.setText( departure.getScheduledDeparture() );
    }
  }
}
