package com.shahardror.wetrip;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripsViewHolder> {

    private List<Trip> tripsList;
    private Context context;
    private TripsListener listener;
    private ArrayList<String> imageUrls;
    private Location myLocation , tripLocation;

    // Constructor
    public TripAdapter(List<Trip> tripsList) {

        this.tripsList = tripsList;
        tripLocation = new Location("WeTripLoc");
    }

    public interface TripsListener {
        void onTripsClick(int position, View view);
    }


    public void setListener(TripsListener tripsListener) {
        this.listener = tripsListener;

    }

    public void setTripsList(List<Trip> tripsList) {
        this.tripsList = tripsList;
    }

    public class TripsViewHolder extends RecyclerView.ViewHolder {

        ImageView attraction_image;
        TextView        trip_name,
                        attraction_name,
                        time_tv,
                        date_tv,
                        distanceTv,
                        numOfPartTv;
        // participants?;

        public TripsViewHolder(@NonNull View itemView) {
            super(itemView);

            attraction_image = itemView.findViewById(R.id.trip_card_attraction_image);
            trip_name = itemView.findViewById(R.id.trip_card_trip_name);
            attraction_name = itemView.findViewById(R.id.trip_card_attraction_name);
            time_tv = itemView.findViewById(R.id.trip_card_time_tv);
            date_tv = itemView.findViewById(R.id.trip_card_date_tv);
            distanceTv=itemView.findViewById(R.id.trip_card_distance);
            numOfPartTv = itemView.findViewById(R.id.trip_card_num_of_part);

            itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onTripsClick(getAdapterPosition(),v);
                }
            });
        }


    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();


    }

    public void clearAdapterList(){
        tripsList.clear();
    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trip_card,parent,false);
        TripsViewHolder holder = new TripsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
        Trip trip = this.tripsList.get(position);

        holder.trip_name.setText(trip.getTitle());
        holder.attraction_name.setText(trip.getAttractionName());
        holder.date_tv.setText(trip.getMeetingDate());
        holder.time_tv.setText(trip.getMeetingTime());

        if(trip.getParticipantsID() !=null)
         holder.numOfPartTv.setText((trip.getParticipantsID().size() + 1) + "/5");
        else
            holder.numOfPartTv.setText("1/5");

        tripLocation.setLongitude(trip.longitudeOfAttraction);
        tripLocation.setLatitude(trip.getLatitudeOfAttraction());
       // myLocation =  CurrentLocationListener.getInstance(context).loc;
        if(MainActivity.myLoc != null)
          holder.distanceTv.setText(( (int)tripLocation.distanceTo(MainActivity.myLoc)/1000) + s(R.string.km)  );
        else
            holder.distanceTv.setText(s(R.string.zero_km));
        Glide.with(context).load(trip.getImageURL()).into(holder.attraction_image);
    }

    private String s(int id)
    {
        if(context != null)
          return context.getResources().getString(id);
        else return "";
    }
    @Override
    public int getItemCount() { return tripsList.size(); }
}
