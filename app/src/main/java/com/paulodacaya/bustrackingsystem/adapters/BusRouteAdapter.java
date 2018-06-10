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
import com.paulodacaya.bustrackingsystem.ui.AnalysisGraphActivity;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

import java.util.List;

public class BusRouteAdapter extends RecyclerView.Adapter<BusRouteAdapter.BusRouteViewHolder> {

  private Context mContext;
  private List<String> mBusRouteList;

  public BusRouteAdapter(Context context, List<String> busRouteList) {
    mContext = context;
    mBusRouteList = busRouteList;
  }

  @NonNull
  @Override
  public BusRouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View view = LayoutInflater.from( parent.getContext() )
            .inflate( R.layout.bus_route_list_item, parent, false );

    BusRouteViewHolder viewHolder = new BusRouteViewHolder( view, mContext );

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BusRouteViewHolder holder, int position) {
    String busRoute = mBusRouteList.get( position );
    holder.bindBusRoute( busRoute );
  }

  @Override
  public int getItemCount() {
    return mBusRouteList.size();
  }


  public class BusRouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mBusRouteLabel;
    private String mBusRoute;

    public BusRouteViewHolder( View itemView, Context context ) {
      super(itemView);

      mContext = context;
      mBusRouteLabel = itemView.findViewById( R.id.busRouteLabel );

      itemView.setOnClickListener( this ); // register on click lister for
    }

    public void bindBusRoute( String route ) {

      mBusRoute = route; // keep original string for next activity e.g. 'Bus_456'

      route = route.replace( "_", " " ); // e.g. 'Bus 456' for better visual
      mBusRouteLabel.setText( route );
    }

    @Override
    public void onClick(View v) {

      // start analysis activity with the bus Route passed
      Intent intent = new Intent( mContext, AnalysisGraphActivity.class );
      intent.putExtra( Constants.BUS_ROUTE, mBusRoute );
      mContext.startActivity( intent );
    }
  }
}
