package com.shahardror.wetrip;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.ALARM_SERVICE;

public class SearchFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference friendsTable = database.getReference("friends");
    DatabaseReference tripsTable = database.getReference("trips");
    DatabaseReference usersTable = database.getReference("users");
    ArrayList<Trip> trips = new ArrayList<>();
    RecyclerView recyclerView;
    TripAdapter adapter;
    boolean firstTime = true;
    AlertDialog   dialogViewOfTrip ;
    double lastDistance;
    Context context;
    Location myLocation , tripLocation = new Location("tripLocation");
   // double latitude, longitude;
    AlarmManager manager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.activity_feed, container, false);

        recyclerView = root.findViewById(R.id.recycler);
        firebaseAuth = FirebaseAuth.getInstance();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        context = getContext();
        if(getContext() != null)
            manager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        // refreshFeed();

        return root;
    }

    public void getLoc(final double dis){

        lastDistance = dis;

            Log.i("tag" , "not null");
            CurrentLocationListener.getInstance(getContext()).observe(this, new Observer<Location>() {
                @Override
                public void onChanged(Location location) {
                    Log.i("tag" , "never comes here");
                    if(location != null){
                        myLocation = location;
                        Log.i("tag" , location.toString());
                        if(firstTime && dis != 0){
                            refreshFeed(dis);
                            firstTime = false;
                        }


                    }
                }
            });


    }

    public void refreshFeed(final double dis) {


        tripsTable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    for (DataSnapshot dst: ds.getChildren()) {

                        tripLocation.setLatitude(Double.parseDouble(dst.child("latitudeOfAttraction").getValue().toString()));
                        tripLocation.setLongitude(Double.parseDouble(  dst.child("longitudeOfAttraction").getValue().toString()));

                        double distanceBetween = (double) myLocation.distanceTo(tripLocation);

                        if(dis > distanceBetween){ //dis > myLocation.distanceTo(tripLocation

                            ArrayList<String> par = new ArrayList<>();

                            if(dst.hasChild("participantsID"))
                                for (DataSnapshot parDs : dst.child("participantsID").getChildren()){
                                    par.add(parDs.getValue().toString());
                                }


                            trips.add( new Trip(
                                    dst.child("attractionName").getValue().toString(),
                                    dst.child("title").getValue().toString(),
                                    dst.child("userID").getValue().toString(),
                                    dst.child("meetingTime").getValue().toString(),
                                    dst.child("meetingDate").getValue().toString(),
                                    dst.child("meetingPoint").getValue().toString(),
                                    dst.child("extraDetails").getValue().toString(),
                                    Long.parseLong(dst.child("timePosted").getValue().toString()),
                                    dst.child("imageURL").getValue().toString(),
                                    par ,
                                    Double.parseDouble(  dst.child("longitudeOfAttraction").getValue().toString()),
                                    Double.parseDouble(dst.child("latitudeOfAttraction").getValue().toString())
                            ) );
                        }
                    }
                }

                Collections.sort(trips);

                adapter = new TripAdapter(trips);
                recyclerView.setAdapter(adapter);



                adapter.setListener(new TripAdapter.TripsListener() {
                    @Override
                    public void onTripsClick(final int position, View view) {

                        if (firebaseAuth.getCurrentUser() == null) {
                            Log.i("login", "Please log in!");
//                            NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
//                            navigationView.setCheckedItem(R.id.item_login);
                            CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.coordinator_layout);
                            Snackbar.make(coordinatorLayout,s(R.string.need_login),Snackbar.LENGTH_LONG).setAction(s(R.string.login), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.floating_button);
                                    floatingActionButton.performClick();
                                }
                            }).show();
                            return;
                        }



                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        final View dialogView = getLayoutInflater().inflate(R.layout.trip_extension_details_layout, null);
                        ImageView imageAttraction = dialogView.findViewById(R.id.image_attraction_trip_extension);
                        final ImageView profileImageAdmin = dialogView.findViewById(R.id.admin_image_trip_extension);
                        Glide.with(dialogView.getContext()).load(trips.get(position).getImageURL()).into(imageAttraction);
                        final TextView creatorName = dialogView.findViewById(R.id.profile_name_trip_extension);
                        final String imageStr = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F" + trips.get(position).getUserID() + ".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";


                        final Button  delete = dialogView.findViewById(R.id.delete_trip_extension);
                        final Button  leave = dialogView.findViewById(R.id.leave_trip_extension);
                        final Button  join = dialogView.findViewById(R.id.join_trip_extension);
                        final Button full = dialogView.findViewById(R.id.full_trip_extension);
                        final String myId = firebaseAuth.getCurrentUser().getUid();
                        String tripAdminId = trips.get(position).getUserID();

                        final CircleImageView imageViewProfile1 = dialogView.findViewById(R.id.profile1_trip_extension);
                        final  CircleImageView imageViewProfile2 = dialogView.findViewById(R.id.profile2_trip_extension);
                        final  CircleImageView imageViewProfile3 = dialogView.findViewById(R.id.profile3_trip_extension);
                        final  CircleImageView imageViewProfile4 = dialogView.findViewById(R.id.profile4_trip_extension);

                        TextView title_trip_extension = dialogView.findViewById(R.id.title_trip_extension);
                        TextView name_attraction_trip_extension = dialogView.findViewById(R.id.name_attraction_trip_extension);
                        TextView meeting_location_trip_extension = dialogView.findViewById(R.id.meeting_location_trip_extension);
                        TextView extra_details_trip_extension = dialogView.findViewById(R.id.extra_details_trip_extension);

                        setClickablePopUp(title_trip_extension,trips.get(position).getTitle());
                        setClickablePopUp(name_attraction_trip_extension,trips.get(position).getAttractionName());
                        setClickablePopUp(meeting_location_trip_extension,trips.get(position).getMeetingPoint());
                        setClickablePopUp(extra_details_trip_extension,trips.get(position).getExtraDetails());

                        if(trips.get(position).participantsID != null){
                            //   downloadAllImages(trips.get(position).participantsID);



                            int flag = -1;
                            for (int i = 0 ; i < trips.get(position).getParticipantsID().size() ; i++){
                                if(firebaseAuth.getCurrentUser().getUid().equals(trips.get(position).getParticipantsID().get(i)) ){
                                    flag = i;
                                    break;
                                }
                            }
                            if(flag != -1){
                                Collections.swap(trips.get(position).getParticipantsID() , trips.get(position).getParticipantsID().size() -1 , flag );
                            }

                            ArrayList<String> uidImages = trips.get(position).participantsID;
                            //final String strImage = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F" + uidImages.get(i) + ".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";
                            ArrayList<String> linkToImages = new ArrayList<>();
                            for (int i = 0 ; i < uidImages.size() ; i++){
                                linkToImages.add("https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F" + uidImages.get(i) + ".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249");
                            }

                            if(linkToImages.size() > 0){
                                Glide.with(context).applyDefaultRequestOptions(new RequestOptions().error(R.drawable.ic_person_gray_72dp)).load(linkToImages.get(0)).into(imageViewProfile1);
                            }else{
                                //  imageViewProfile1.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_gray_72dp));
                            }
                            if(linkToImages.size() > 1){
                                Glide.with(context).load(linkToImages.get(1)).into(imageViewProfile2);
                            }else {
                                //imageViewProfile2.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_gray_72dp));
                            }
                            if(linkToImages.size() > 2){
                                Glide.with(context).load(linkToImages.get(2)).into(imageViewProfile3);
                            }else {
                                // imageViewProfile3.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_gray_72dp));
                            }
                            if(linkToImages.size() > 3){
                                Glide.with(context).load(linkToImages.get(3)).into(imageViewProfile4);
                            }else {
                                // imageViewProfile4.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_gray_72dp));
                            }
                        }
                        imageViewProfile1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (trips.get(position).getParticipantsID() != null  && trips.get(position).getParticipantsID().size() > 0 ){
                                    // Move to Profile
                                    moveToProfileFragment( trips.get(position).getParticipantsID().get(0) , firebaseAuth.getCurrentUser().getUid() );
                                }
                            }
                        });
                        imageViewProfile2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (trips.get(position).getParticipantsID() != null  && trips.get(position).getParticipantsID().size() > 1 ){
                                    // Move to Profile
                                    moveToProfileFragment(trips.get(position).getParticipantsID().get(1) , firebaseAuth.getCurrentUser().getUid() );
                                }
                            }
                        });
                        imageViewProfile3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (trips.get(position).getParticipantsID() != null  && trips.get(position).getParticipantsID().size() > 2 ){
                                    // Move to Profile
                                    moveToProfileFragment(trips.get(position).getParticipantsID().get(2) , firebaseAuth.getCurrentUser().getUid() );
                                }
                            }
                        });
                        imageViewProfile4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (trips.get(position).getParticipantsID() != null  && trips.get(position).getParticipantsID().size() > 3 ){
                                    // Move to Profile
                                    moveToProfileFragment(trips.get(position).getParticipantsID().get(3) , firebaseAuth.getCurrentUser().getUid() );
                                }
                            }
                        });




                        boolean iAmInThisTrip = false;

                        if (myId.equals(tripAdminId)) {  // Im Admin
                            delete.setVisibility(View.VISIBLE);
                            leave.setVisibility(View.GONE);
                            join.setVisibility(View.GONE);
                        } else {

                            if (trips.get(position).participantsID != null) {
                                ArrayList<String> participantsUids = trips.get(position).participantsID;

                                for (int i = 0; i < participantsUids.size(); i++) {
                                    if (participantsUids.get(i).equals(myId)) {
                                        iAmInThisTrip = true;
                                        break;
                                    }
                                }

                                if (iAmInThisTrip) {
                                    delete.setVisibility(View.GONE);
                                    leave.setVisibility(View.VISIBLE);
                                    join.setVisibility(View.GONE);
                                    full.setVisibility(View.GONE);
                                } else { // Full Or Join
                                    if (participantsUids.size() == 4) {// Full
                                        full.setVisibility(View.VISIBLE);
                                        delete.setVisibility(View.GONE);
                                        leave.setVisibility(View.GONE);
                                        join.setVisibility(View.GONE);
                                    } else {// Join
                                        full.setVisibility(View.GONE);
                                        delete.setVisibility(View.GONE);
                                        leave.setVisibility(View.GONE);
                                        join.setVisibility(View.VISIBLE);
                                    }
                                }

                            }
                        }




                        usersTable.child( trips.get(position).getUserID() ).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.hasChild("name")){
                                    creatorName.setText(dataSnapshot.child("name").getValue().toString());
                                }

                                if  ( dataSnapshot.hasChild("photo")){
                                    final DataSnapshot dataSnapshot1 = dataSnapshot.child("photo");
                                    RequestQueue requestQueueImage = Volley.newRequestQueue(context);
                                    ImageRequest imageRequest = new ImageRequest(imageStr, new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            if (response != null){
                                                if(dataSnapshot1.child("fromExternal").getValue().toString().equals("false")){
                                                    response = RotateBitmap(response , 90);
                                                }
                                                profileImageAdmin.setImageBitmap(response);

                                            }
                                        }
                                    }, 2000, 2000, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    });
                                    requestQueueImage.add(imageRequest);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        TextView attNameTv = dialogView.findViewById(R.id.name_attraction_trip_extension);

                        TextView titleNameTv = dialogView.findViewById(R.id.title_trip_extension);
                        TextView timeAttTv = dialogView.findViewById(R.id.time_trip_extension);
                        TextView dateAttTv = dialogView.findViewById(R.id.date_trip_extension);
                        TextView meetingPointTv = dialogView.findViewById(R.id.meeting_location_trip_extension);
                        TextView extraDetailsTv = dialogView.findViewById(R.id.extra_details_trip_extension);


                        TextView distanceTv = dialogView.findViewById(R.id.distance_trip_extension);
                        tripLocation.setLongitude(trips.get(position).getLongitudeOfAttraction());
                        tripLocation.setLatitude(trips.get(position).getLatitudeOfAttraction());
                        if(myLocation != null)
                            distanceTv.setText(( (int)tripLocation.distanceTo(myLocation)/1000) + "Km" );
                        else
                            distanceTv.setText("0Km");



                       final TextView numParticipantsTv = dialogView.findViewById(R.id.num_of_participants_trip_extension);

                        if(trips.get(position).getParticipantsID() != null)
                            numParticipantsTv.setText((trips.get(position).getParticipantsID().size()+ 1)+"/5");
                        else
                            numParticipantsTv.setText("0/5");


                        attNameTv.setText(trips.get(position).getAttractionName());
                        //crearorName.setText(trips.get(position).get);
                        titleNameTv.setText(trips.get(position).getTitle());
                        timeAttTv.setText(trips.get(position).getMeetingTime());
                        dateAttTv.setText(trips.get(position).getMeetingDate());
                        meetingPointTv.setText(trips.get(position).getMeetingPoint());
                        extraDetailsTv.setText(trips.get(position).getExtraDetails());

                        builder.setView(dialogView);
                        final AlertDialog bigImageDialog = builder.create();
                        dialogViewOfTrip = bigImageDialog;
                        bigImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        bigImageDialog.show();
                        final String clickedUserIdStr = trips.get(position).getUserID();
                        final String currUserIdStr = firebaseAuth.getCurrentUser().getUid();
                        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){

                                            case DialogInterface.BUTTON_POSITIVE:

                                                if(getContext() != null){
                                                    int reqCode = (int)trips.get(position).getTimePosted();
                                                    Intent intent = new Intent(getContext(), AlarmReceiver.class);
                                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                                    manager.cancel(pendingIntent);
                                                    Log.i("tag" , "trip canceled");
                                                }

                                                tripsTable.child(firebaseAuth.getCurrentUser().getUid()).child(trips.get(position).getTitle()).getRef().removeValue();
                                                FragmentTransaction fragmentTransaction;
                                                //create fragment transaction
                                                fragmentTransaction = fragmentManager.beginTransaction();
                                               final SearchFragment searchFragment = new SearchFragment();
                                                fragmentTransaction.replace(R.id.recycler_container, searchFragment);
                                                fragmentTransaction.commit();
                                                fragmentTransaction.addToBackStack("searchFragment");

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (searchFragment.isAdded())
                                                        searchFragment.getLoc(lastDistance);
                                                    }
                                                },400);

                                                bigImageDialog.dismiss();

                                                leave.setVisibility(View.GONE);
                                                join.setVisibility(View.VISIBLE);

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                dialog.dismiss();
                                                break;
                                        }
                                    }
                                };

                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setMessage(s(R.string.delete_trip))
                                        .setPositiveButton(s(R.string.delete), dialogClickListener)
                                        .setNegativeButton(s(R.string.keep), dialogClickListener)
                                        .show();

                            }
                        });
                        leave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){

                                            case DialogInterface.BUTTON_POSITIVE:

                                                if(getContext() != null){
                                                    int reqCode = (int)trips.get(position).getTimePosted();
                                                    Intent intent = new Intent(getContext(), AlarmReceiver.class);
                                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                                    manager.cancel(pendingIntent);
                                                    Log.i("tag" , "trip canceled");
                                                }


                                                tripsTable.child(trips.get(position).getUserID()).child(trips.get(position).title).child("participantsID").child(myId).getRef().removeValue();
                                                FragmentTransaction fragmentTransaction;
                                                //create fragment transaction
                                                fragmentTransaction = fragmentManager.beginTransaction();
                                                final SearchFragment searchFragment = new SearchFragment();
                                                fragmentTransaction.replace(R.id.recycler_container, searchFragment);
                                                fragmentTransaction.commit();
                                                fragmentTransaction.addToBackStack("searchFragment");

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (searchFragment.isAdded())
                                                            searchFragment.getLoc(lastDistance);
                                                    }
                                                },400);

                                                String  numStr = numParticipantsTv.getText().toString();
                                                numStr = numStr.substring(0,1);
                                                int num = Integer.parseInt(numStr);
                                                num--;
                                                numParticipantsTv.setText(num + "/5");

                                                switch (--num){
                                                    case 0:

                                                        imageViewProfile1.setImageBitmap(null);
                                                        break;
                                                    case 1:

                                                        imageViewProfile2.setImageBitmap(null);
                                                        break;
                                                    case 2:

                                                        imageViewProfile3.setImageBitmap(null);
                                                        break;
                                                    case 3:

                                                        imageViewProfile4.setImageBitmap(null);
                                                        break;
                                                }

                                                leave.setVisibility(View.GONE);
                                                join.setVisibility(View.VISIBLE);

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                dialog.dismiss();
                                                break;
                                        }
                                    }
                                };

                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setMessage(s(R.string.leave_trip))
                                        .setPositiveButton(s(R.string.leave), dialogClickListener)
                                        .setNegativeButton(s(R.string.keep), dialogClickListener)
                                        .show();

                            }
                        });
                        join.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                String dateString = trips.get(position).meetingDate;
                                String[] separatedDate = dateString.split("/");
                                String timeString = trips.get(position).meetingTime;
                                String[] separatedTime = timeString.split(":");
                                int year, month , date , hourTrip , minute;
                                year = Integer.parseInt(separatedDate[2]);
                                month = Integer.parseInt(separatedDate[1]);
                                date = Integer.parseInt(separatedDate[0]);
                                hourTrip = Integer.parseInt(separatedTime[0]);
                                minute = Integer.parseInt(separatedTime[1]);
                                Calendar calendarTrip = Calendar.getInstance();
                                month--;
                                calendarTrip.set(year , month ,date,hourTrip,minute);


                                Log.i("tag" , "year:  " + year + "  :  "+"month:  " + month + "  :  "+"date:  " + date + "  :  "+"hourTrip:  " + hourTrip + "  :  "+"minute:  " + minute );
                                Log.i("tag" ,   "time of trip" +  calendarTrip.getTimeInMillis() );
                                Calendar calendar1 = Calendar.getInstance();
                                long timeOfTrip = calendarTrip.getTimeInMillis();
                                long day = 24*60*60*1000;
                                long hour = 60*60*1000;
                                long timeOfAlert = (timeOfTrip - day);
                                long timeNow = calendar1.getTimeInMillis();
                                long range = timeOfTrip - timeNow;


                                if(range < 0 && getContext() != null){
                                    Toast.makeText(getContext(), s(R.string.cant_join_trip_past), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int reqCode = (int)trips.get(position).getTimePosted();
                                if(range < day){
                                    if(range > hour){
                                        timeOfAlert = (timeOfTrip - hour);
                                        Intent intent = new Intent(getContext(), AlarmReceiver.class);
                                        intent.putExtra("reqCode", reqCode);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                        manager.setExact(AlarmManager.RTC_WAKEUP, timeOfAlert , pendingIntent);
                                        Log.i("tag" , "hour before");
                                    }else{
                                        Log.i("tag" , "not set");
                                    }
                                }else{
                                    Intent intent = new Intent(getContext(), AlarmReceiver.class);
                                    intent.putExtra("reqCode", reqCode);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                    manager.setExact(AlarmManager.RTC_WAKEUP, timeOfAlert , pendingIntent);
                                    Log.i("tag" , "day before");
                                }










                                Log.i("jojo" , "yo clicked");
                                tripsTable.child(trips.get(position).getUserID()).child(trips.get(position).title).child("participantsID").child(myId).setValue(myId);
                                // bigImageDialog.dismiss();
                                FragmentTransaction fragmentTransaction;
                                //create fragment transaction
                                fragmentTransaction = fragmentManager.beginTransaction();
                                final SearchFragment searchFragment = new SearchFragment();
                                fragmentTransaction.replace(R.id.recycler_container, searchFragment);
                                fragmentTransaction.commit();
                                fragmentTransaction.addToBackStack("searchFragment");

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (searchFragment.isAdded())
                                            searchFragment.getLoc(lastDistance);
                                    }
                                },400);

                                String urlToMtImage = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F" +firebaseAuth.getCurrentUser().getUid() + ".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";

                                String  numStr = numParticipantsTv.getText().toString();
                                numStr = numStr.substring(0,1);
                                int num = Integer.parseInt(numStr);
                                num++;
                                numParticipantsTv.setText(num + "/5");
                                num--;
                                switch (--num) {
                                    case 0:
                                        if (getContext() != null)
                                            Glide.with(getContext()).load(urlToMtImage).into(imageViewProfile1);
                                        break;
                                    case 1:
                                        if (getContext() != null)
                                            Glide.with(getContext()).load(urlToMtImage).into(imageViewProfile2);
                                        break;
                                    case 2:
                                        if (getContext() != null)
                                            Glide.with(getContext()).load(urlToMtImage).into(imageViewProfile3);
                                        break;
                                    case 3:
                                        if (getContext() != null)
                                            Glide.with(getContext()).load(urlToMtImage).into(imageViewProfile4);
                                        break;
                                }

                                    leave.setVisibility(View.VISIBLE);
                                join.setVisibility(View.GONE);


                            }
                        });



                        profileImageAdmin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(clickedUserIdStr.equals(currUserIdStr)){

                                    FragmentTransaction fragmentTransaction;
                                    //create fragment transaction
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    ProfileFragment profileFragment = new ProfileFragment();
                                    fragmentTransaction.replace(R.id.recycler_container, profileFragment);
                                    fragmentTransaction.commit();
                                    fragmentTransaction.addToBackStack("profileFragment");
                                    bigImageDialog.dismiss();
                                }else{
                                    FragmentTransaction fragmentTransaction;
                                    //create fragment transaction
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    OtherProfileFragment otherProfileFragment = new OtherProfileFragment();
                                    fragmentTransaction.replace(R.id.recycler_container, otherProfileFragment);
                                    fragmentTransaction.commit();
                                    otherProfileFragment.downloadProfileImage(clickedUserIdStr);
                                    fragmentTransaction.addToBackStack("otherProfileFragment");
                                    bigImageDialog.dismiss();
                                }

                            }
                        });


                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void moveToProfileFragment(String clickedUserIdStr ,String currUserIdStr ){

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if(clickedUserIdStr.equals(currUserIdStr)){

            FragmentTransaction fragmentTransaction;
            //create fragment transaction
            fragmentTransaction = fragmentManager.beginTransaction();
            ProfileFragment profileFragment = new ProfileFragment();
            fragmentTransaction.replace(R.id.recycler_container, profileFragment);
            fragmentTransaction.commit();
            fragmentTransaction.addToBackStack("profileFragment");
            dialogViewOfTrip.dismiss();

        }else{
            FragmentTransaction fragmentTransaction;
            //create fragment transaction
            fragmentTransaction = fragmentManager.beginTransaction();
            OtherProfileFragment otherProfileFragment = new OtherProfileFragment();
            fragmentTransaction.replace(R.id.recycler_container, otherProfileFragment);
            fragmentTransaction.commit();
            otherProfileFragment.downloadProfileImage(clickedUserIdStr);
            fragmentTransaction.addToBackStack("otherProfileFragment");
            dialogViewOfTrip.dismiss();
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, source.getWidth(), source.getHeight(), true);

        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.floating_button).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.VISIBLE);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.item_feed);
        MainActivity.actionBar.setTitle("WeTrip");
    }

    private String s(int id)
    {
        return this.getResources().getString(id);
    }
    @Override
    public void onStop() {
        super.onStop();
        getActivity().findViewById(R.id.floating_button).setVisibility(View.GONE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.GONE);
    }

    private void setClickablePopUp(final TextView textView, final String message)
    {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.popUp(getContext(),message);
            }
        });
    }
}


//                    Log.i("tag" , "distance is:" + dis);
//                    Log.i("tag" , location.toString());
//
//                    Location tripLocation = new Location("tripLocation");
//
//                    for (int i = 0 ; i < trips.size() ; i++){
//                        tripLocation.setLatitude(trips.get(i).latitudeOfAttraction);
//                        tripLocation.setLongitude(trips.get(i).longitudeOfAttraction);
//
//                        Log.i("tag" , "tripLocation in position: " + i + " is: " + tripLocation.toString());
//                        Log.i("tag" , "distance between is: "+ tripLocation.distanceTo(location));
//                        if(dis < tripLocation.distanceTo(location)){
//                            trips.remove(trips.get(i));
//                            adapter.notifyItemRemoved(i);
//                            adapter.notifyItemRangeChanged(i , trips.size());
//                        }

//                   }