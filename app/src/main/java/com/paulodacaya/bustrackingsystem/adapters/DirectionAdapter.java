package com.paulodacaya.bustrackingsystem.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paulodacaya.bustrackingsystem.R;
import com.paulodacaya.bustrackingsystem.timetable.Direction;
import com.paulodacaya.bustrackingsystem.ui.DriverMapsActivity;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

public class DirectionAdapter extends RecyclerView.Adapter<DirectionAdapter.DirectionViewHolder>  {

  private Direction[] mDirections;
  private Context mContext;

  public DirectionAdapter( Context context, Direction[] directions) {
    mContext = context;
    mDirections = directions;
  }

  @NonNull
  @Override
  public DirectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View view = LayoutInflater.from( parent.getContext() )
            .inflate( R.layout.route_direction_list_item, parent, false );

    DirectionViewHolder viewHolder = new DirectionViewHolder( view, mContext );

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DirectionViewHolder holder, int position) {
    holder.bindDirection( mDirections[position] );
  }

  @Override
  public int getItemCount() {
    return mDirections.length;
  }



  public class DirectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mRouteDirectionLabel;

    private int mDirectionId;
    private int mRouteId;
    private String mBusNumber;

    public DirectionViewHolder( View itemView, Context context ) {
      super(itemView);

      mContext = context;
      mRouteDirectionLabel = itemView.findViewById( R.id.endingBusLabel);

      itemView.setOnClickListener( this ); // register onclick listener
    }

    public void bindDirection( Direction direction ) {
      mRouteDirectionLabel.setText( "To " + direction.getDirectionName() );

      mDirectionId = direction.getDirectionId();
      mRouteId = direction.getRouteId();
      mBusNumber = direction.getBusNumber();
    }

    @Override
    public void onClick(View v) {

      // HANDLE THE CLICK on List item, and start new Maps Activity, send route_id from selected List Item
      Intent intent = new Intent( mContext, DriverMapsActivity.class );
      intent.putExtra( Constants.ROUTE_ID, mRouteId + "" );
      intent.putExtra( Constants.DIRECTION_ID, mDirectionId + "" );
      intent.putExtra( Constants.BUS_NUMBER, mBusNumber ); // send bus number

      // Start another to get direction ID
      mContext.startActivity( intent );

    }
  }
}
