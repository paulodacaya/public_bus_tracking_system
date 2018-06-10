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
import com.paulodacaya.bustrackingsystem.timetable.Route;
import com.paulodacaya.bustrackingsystem.ui.RouteDirectionActivity;
import com.paulodacaya.bustrackingsystem.utilities.Constants;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

  private Route[] mRoutes;
  private Context mContext;

  public SearchAdapter(Context context, Route[] routes) {
    mContext = context;
    mRoutes = routes;
  }

  @NonNull
  @Override
  public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View view = LayoutInflater.from( parent.getContext() )
            .inflate( R.layout.search_list_item, parent, false );

    SearchViewHolder viewHolder = new SearchViewHolder( view, mContext );

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
    holder.bindSearch( mRoutes[position] );
  }

  @Override
  public int getItemCount() {
    return mRoutes.length;
  }

  public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mRouteNameLabel;
    public TextView mRouteNumberLabel;

    private int mRouteId;

    public SearchViewHolder(View itemView, Context context ) {
      super(itemView);

      mContext = context;
      mRouteNameLabel = itemView.findViewById( R.id.routeNameLabel );
      mRouteNumberLabel = itemView.findViewById( R.id.endingBusLabel);

      itemView.setOnClickListener( this ); // register onclick listener
    }

    public void bindSearch( Route route ) {
      mRouteNameLabel.setText( route.getRouteName() );
      mRouteNumberLabel.setText( route.getRouteNumber() );
      mRouteId = route.getRouteId();
    }

    @Override
    public void onClick(View v) {

      // HANDLE THE CLICK on List item, and start new Maps Activity, send route_id from selected List Item
      Intent intent = new Intent( mContext, RouteDirectionActivity.class );
      intent.putExtra( Constants.ROUTE_ID, mRouteId + "" );                      // convert to string
      intent.putExtra( Constants.BUS_NUMBER, mRouteNumberLabel.getText().toString() ); // send bus number

      // Start another to get direction ID
      mContext.startActivity( intent );

    }
  }
}
