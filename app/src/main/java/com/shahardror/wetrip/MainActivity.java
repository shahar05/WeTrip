package com.shahardror.wetrip;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class MainActivity extends AppCompatActivity {
    final int LOCATION_PERMISSION_REQUEST = 1;
    //   ===== Fragments ====
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    CoordinatorLayout coordinatorLayout;

    private FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    //FireBaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersTable = database.getReference("users");
    DatabaseReference tripsTable = database.getReference("trips");

    //FCM
    public static String localToken;
    public static String localUserID;

    public static SafeToast safeToast = new SafeToast();

    double longitudeOfAttraction , latitudeOfAttraction;
    String photoUrlOfAtt = "";
    PlacesClient placesClient;
    String API_KEY = "AIzaSyCOB1T6al-uk9lYPFLTg9M2J8eGUTXGfQ8";

    View newTripDialogView;

    Calendar calendar;
    AlarmManager manager;
   public static Location myLoc;
//calander
    int DaySelected , MounthSelected , YearSelected ,HourSelected  , MinuteSelected;

     // number pickar value
        int numPickerValue = 0;
    // SeekBar
    double dis = 0;

    //backpressed:
    int fragmentCount = 0;

    BroadcastReceiver receiver;

    public static ActionBar actionBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null)
            localUserID = firebaseAuth.getCurrentUser().getUid();

        final Button searchBtn = findViewById(R.id.find_appropriate_trips_btn_main_activity);
        FloatingActionButton floatingActionButton = findViewById(R.id.floating_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        Button refreshBtn = findViewById(R.id.refresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedItemClicked();
            }
        });

        feedItemClicked();

        if (getIntent().hasExtra("ChatNotif"))
        {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction;
            //create fragment transaction
            fragmentTransaction = fragmentManager.beginTransaction();
            ChatFragment chatFragment = new ChatFragment();
            fragmentTransaction.replace(R.id.recycler_container, chatFragment);
            fragmentTransaction.commit();
            fragmentTransaction.addToBackStack("chatFragment");
            chatFragment.showChat(firebaseAuth.getCurrentUser().getUid(), getIntent().getStringExtra("ChatNotif"));
        }
            Log.i("FCM_MAIN_ACTIVITY_NOTIF", "got message from "+getIntent().getStringExtra("ChatNotif"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
          getLoc();
        }


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.item_login:
                        loginItemClicked();
                        break;
                    case R.id.item_register:
                        registerItemClicked();
                        break;
                    case R.id.item_feed:
                        feedItemClicked();
                        break;
                    case R.id.item_logout:
                        navigationView.setCheckedItem(R.id.item_feed);
                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(null);
                        firebaseAuth.signOut();
                        feedItemClicked();
                        break;
                    case R.id.item_my_trips:
                        if (firebaseAuth.getCurrentUser() != null)
                            myTripsItemClicked();
                        break;
                    case R.id.item_profile:
                        profileItemClicked();
                        break;
                    case R.id.item_friend:
                        friendItemClicked();
                        break;
                }
                return false;
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                View header_view = navigationView.getHeaderView(0);
                TextView header_layout_username_tv = header_view.findViewById(R.id.header_layout_username_tv);
                if (firebaseAuth.getCurrentUser() != null) {
                    //once logged in:
                    navigationView.getMenu().findItem(R.id.item_login).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_register).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_my_trips).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_logout).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_profile).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_friend).setVisible(true);
                    header_layout_username_tv.setText(firebaseAuth.getCurrentUser().getDisplayName());
                } else {
                    //when not logged in:
                    navigationView.getMenu().findItem(R.id.item_login).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_register).setVisible(true);
                    navigationView.getMenu().findItem(R.id.item_logout).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_my_trips).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_profile).setVisible(false);
                    navigationView.getMenu().findItem(R.id.item_friend).setVisible(false);
                    header_layout_username_tv.setText(s(R.string.logout));
                }
            }
        };

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception ex) {}

                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch(Exception ex) {}

                if(!gps_enabled && !network_enabled) {
                    // notify user
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(s(R.string.open_gps))
                            .setPositiveButton(s(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                            )   .setNegativeButton(s(R.string.cancel),null)
                                    .show();
                    return;
                }



                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View  dialogView = getLayoutInflater().inflate(R.layout.search_dialog, null);
                Button button = dialogView.findViewById(R.id.search_dialog_search_btn);
               final SeekBar seekBarDialog = dialogView.findViewById(R.id.search_dialog_seek_bar);
              final  TextView distanceTv = dialogView.findViewById(R.id.distance_is_search_dialog);
               final ImageView imageView = dialogView.findViewById(R.id.logo_search);
              dis=0;
                seekBarDialog.setMax(300);

                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Window window = dialog.getWindow();
                window.setGravity(Gravity.TOP);

                dialog.show();

                seekBarDialog.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        dis = (double) progress;

                        if(dis == 300){
                            distanceTv.setText(s(R.string.Unlimited));
                        }else
                        distanceTv.setText(progress + s(R.string.km) +"");

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Animation animation = AnimationUtils.loadAnimation(MainActivity.this , R.anim.rotate_image);

                        imageView.startAnimation(animation);

                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {


                                if(dis == 300){

                                    fragmentManager = getSupportFragmentManager();
                                    //create fragment transaction
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    FeedFragment feedFragment = new FeedFragment();
                                    fragmentTransaction.replace(R.id.recycler_container, feedFragment);
                                    //fragmentTransaction.addToBackStack("feedFragment");
                                    fragmentTransaction.commit();
                                    dialog.dismiss();

                                }else{
                                    fragmentManager = getSupportFragmentManager();
                                    //create fragment transaction
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    final SearchFragment searchFragment = new SearchFragment();
                                    fragmentTransaction.replace(R.id.recycler_container, searchFragment);
                                    fragmentTransaction.addToBackStack("searchFragment");
                                    fragmentTransaction.commit();

                                    Handler handler = new Handler();

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(searchFragment.isAdded()){
                                                searchFragment.getLoc(dis * 1000);
                                            }
                                        }
                                    },300);

                                    dialog.dismiss();
                                }


                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                });

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firebaseAuth.getCurrentUser() == null) {
                    loginItemClicked();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (newTripDialogView == null)
                    newTripDialogView = getLayoutInflater().inflate(R.layout.new_trip_dialog, null);

                final EditText title_et = newTripDialogView.findViewById(R.id.new_trip_dialog_title);

                final LinearLayout dateBtn = newTripDialogView.findViewById(R.id.date_new_trip_dialog);
                final LinearLayout timeBtn  = newTripDialogView.findViewById(R.id.time_new_trip_dialog);
                final EditText time_et = newTripDialogView.findViewById(R.id.new_trip_dialog_time_et);
                final EditText date_et = newTripDialogView.findViewById(R.id.new_trip_dialog_date_et);

                final EditText point_et = newTripDialogView.findViewById(R.id.new_trip_dialog_point);
                final EditText details_et = newTripDialogView.findViewById(R.id.new_trip_dialog_details);
                final TextView attTextView = newTripDialogView.findViewById(R.id.att_name_tv);

                Button submit_btn = newTripDialogView.findViewById(R.id.login_dialog_submit_btn);
                builder.setView(newTripDialogView);
                final AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if (newTripDialogView.getParent() != null) {
                    ((ViewGroup) newTripDialogView.getParent()).removeView(newTripDialogView);
                }
                attTextView.setError(null);
                dialog.show();

                time_et.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("click" , "you clicked");

                        timeBtn.performClick();
                    }
                });
                date_et.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("click" , "you clicked");
                        dateBtn.performClick();
                    }
                });

                dateBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(NewShiftPage.this, "Loading Date Picker...", Toast.LENGTH_SHORT).show();
                        Calendar mcurrentDate = Calendar.getInstance();
                        int mYear = mcurrentDate.get(Calendar.YEAR);
                        int mMonth = mcurrentDate.get(Calendar.MONTH);
                        int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog mDatePicker;
                        mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                // TODO Auto-generated method stub
                                //*      Your code   to get date and time    *//*
                                selectedmonth = selectedmonth + 1;
                                date_et.setText("" + selectedday + "/" + selectedmonth + "/" + selectedyear);

                                DaySelected = selectedday;
                                MounthSelected = selectedmonth - 1 ;
                                YearSelected = selectedyear;
                                //dateChanged = true;
                                //Toast.makeText(MainActivity.this, ("" + selectedday + "/" + selectedmonth + "/" + selectedyear),Toast.LENGTH_SHORT).show();
                            }
                        }, mYear, mMonth, mDay);
                        mDatePicker.setTitle(s(R.string.select_date));
                        mDatePicker.show();
                    }
                });

                timeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Calendar mcurrentTime = Calendar.getInstance();
                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {


                                //calendar.set
                                //fix times:




                                HourSelected = selectedHour;
                                MinuteSelected = selectedMinute;
                                String hourDisplay, minuteDisplay;

                                if (selectedHour < 10) hourDisplay = "0"+selectedHour;
                                else hourDisplay = ""+selectedHour;

                                if (selectedMinute < 10) minuteDisplay = "0"+selectedMinute;
                                else minuteDisplay = ""+selectedMinute;

                                time_et.setText(hourDisplay + ":" + minuteDisplay);
                            }
                        }, hour, minute, true);
                        mTimePicker.setTitle("Select time");
                        mTimePicker.show();
                    }
                });

                // Initialize Places.
                Places.initialize(getApplicationContext(), API_KEY);

                // Create a new Places client instance.
                placesClient = Places.createClient(MainActivity.this);

                // Initialize the AutocompleteSupportFragment.
                final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                        fragmentManager.findFragmentById(R.id.autocomplete_fragment);

                // Specify the types of place data to return.
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

                // Set up a PlaceSelectionListener to handle the response.
                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        // TODO: Get info about the selected place.
                        Log.i("places", "Place: " + place.getName() + ", " + place.getId());
                        getPlaceRequest(place.getId());
                    }
                    @Override
                    public void onError(Status status) {
                        // TODO: Handle the error.
                        Log.i("places", "An error occurred: " + status);
                    }
                });

                autocompleteFragment.setHint(s(R.string.where_to_go));

                submit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                       Calendar  calendarTrip = Calendar.getInstance();
                        calendarTrip.set(YearSelected , MounthSelected , DaySelected , HourSelected,MinuteSelected);

                        Log.i("tag" , calendarTrip.getTimeInMillis() +"");
                        Calendar calendar1 = Calendar.getInstance();
                        Log.i("tag" , calendar1.getTimeInMillis() +"");





                        long timeOfTrip = calendarTrip.getTimeInMillis();
                        long day = 24*60*60*1000;
                        long hour = 60*60*1000;
                        long timeOfAlert = (timeOfTrip - day);
                        long timeNow = calendar1.getTimeInMillis();
                        long range = timeOfTrip - timeNow;

                        String title = title_et.getText().toString();
                        String time = time_et.getText().toString();
                        String date = date_et.getText().toString();
                        String point = point_et.getText().toString();
                        String details = details_et.getText().toString();

                        Log.i("time", "Range = " + range);

                        if(range < 0 ){
                            Toast.makeText(MainActivity.this, s(R.string.past_trip_set), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(attTextView.getText().toString().equals(s(R.string.somewhere))){
                            attTextView.setError(s(R.string.contain_att));
                            return;
                        }
                        if(  TextUtils.isEmpty(title) || TextUtils.isEmpty(time) ||TextUtils.isEmpty(date) ||TextUtils.isEmpty(point) ||TextUtils.isEmpty(details) ){
                            Toast.makeText(MainActivity.this, s(R.string.all_fields), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Date currentTime = Calendar.getInstance().getTime();
                        Log.i("tag", attTextView.getText().toString() + " UID:" + firebaseAuth.getCurrentUser().getUid() + "image " + photoUrlOfAtt);
                        // Take All fields And Create New Trip  then Push It to the DataBase Under The Uid Of The Current User

                        Trip trip = new Trip(attTextView.getText().toString(),
                                title,
                                firebaseAuth.getUid(),
                                time,
                                date,
                                point,
                                details,
                                currentTime.getTime(),
                                photoUrlOfAtt,
                                null ,
                                longitudeOfAttraction ,
                                latitudeOfAttraction);

                        tripsTable.child(trip.getUserID()).child(trip.getTitle()).setValue(trip);
                        feedItemClicked();
                        title_et.setText(null);
                        time_et.setText(null);
                        date_et.setText(null);
                        point_et.setText(null);
                        details_et.setText(null);
                        autocompleteFragment.setText(null);
                        final CircleImageView att_image = newTripDialogView.findViewById(R.id.new_trip_dialog_image);
                        att_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_landscape_gray_60dp));
                        attTextView.setText("Somewhere");

                        dialog.dismiss();


                        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                        intent.putExtra("reqCode" ,(int)currentTime.getTime() );
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,(int)currentTime.getTime() , intent , PendingIntent.FLAG_CANCEL_CURRENT);

                         if(range < day){
                             if(range > hour){
                                 timeOfAlert = (timeOfTrip - hour);
                                 manager.setExact(AlarmManager.RTC_WAKEUP, timeOfAlert , pendingIntent);
                             }
                         }else{
                             manager.setExact(AlarmManager.RTC_WAKEUP, timeOfAlert , pendingIntent);
                         }

                    }
                });
            }
        });

        // FCM:

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM_Main_Actfivity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        localToken = task.getResult().getToken();

                        if(firebaseAuth.getCurrentUser() != null)
                        usersTable.child(firebaseAuth.getCurrentUser()
                                .getUid())
                                .child("token")
                                .setValue(localToken);

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("FCM_Main_Activity", "My token: "+localToken);
                        //Toast.makeText(MainActivity.this, localToken, Toast.LENGTH_SHORT).show();
                    }
                });


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM_Main_Activity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        localToken = task.getResult().getToken();

                        if (firebaseAuth.getCurrentUser() != null) {

                            usersTable.child(firebaseAuth.getCurrentUser()
                                    .getUid())
                                    .child("token")
                                    .setValue(localToken);

                        }

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("FCM_Main_Activity", localToken);
                        //Toast.makeText(MainActivity.this, localToken, Toast.LENGTH_SHORT).show();
                    }
                });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {

                Log.i("Broadcast onReceive", "Context: "+context);
                Log.i("Broadcast onReceive", "Intent: "+intent);
                Log.i("Broadcast onReceive", "From: "+intent.getStringExtra("from"));

                final Button toolbar_chat_bubble = findViewById(R.id.toolbar_chat_bubble);

                if (navigationView.getMenu().findItem(R.id.item_friend).isChecked())
                {
                    Log.i("FCM_receiver", "on friends / chat! DON'T SHOW msg icon on toolbar");
                    toolbar_chat_bubble.setVisibility(View.GONE);
                }

                else
                {
                    Log.i("FCM_receiver", "NOT on friends / chat. SHOW msg icon on toolbar");
                    toolbar_chat_bubble.setVisibility(View.VISIBLE);

                    final  Animation moveStart = AnimationUtils.loadAnimation(MainActivity.this,R.anim.start_btn);
                    toolbar_chat_bubble.startAnimation(moveStart);
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.open);
                    mediaPlayer.start();


                    toolbar_chat_bubble.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction;
                            fragmentTransaction = fragmentManager.beginTransaction();
                            ChatFragment chatFragment = new ChatFragment();
                            fragmentTransaction.replace(R.id.recycler_container, chatFragment);
                            fragmentTransaction.commit();
                            fragmentTransaction.addToBackStack("chatFragment");
                            chatFragment.showChat(firebaseAuth.getCurrentUser().getUid(), intent.getStringExtra("from"));

                            toolbar_chat_bubble.setVisibility(View.GONE);
                        }
                    });

                }
            }
        };


        
        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

    }


    public void getLoc(){
        CurrentLocationListener.getInstance(MainActivity.this).observe(MainActivity.this , new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                myLoc = location;
            }
        });
    }

    private void getPlaceRequest(String placeID) {

        final CircleImageView att_image = newTripDialogView.findViewById(R.id.new_trip_dialog_image);
        final TextView att_tv = newTripDialogView.findViewById(R.id.att_name_tv);

        // Define a Place ID.
        String placeId = placeID;

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS , Place.Field.LAT_LNG);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
                final Place place = response.getPlace();

                // Get the photo metadata.
                if (place.getPhotoMetadatas() == null) {
                    Log.i("places", "failed to get photo metadata! no photo for this place?");
                    return;
                }
                final PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

                // Get the attribution text.
                String attributions = photoMetadata.getAttributions();
                Log.i("places", "Place found: " + place.getName() + place.getPhotoMetadatas() );
                Log.i("tag" , "lat : "+  place.getLatLng().longitude + "lan:" + place.getLatLng().latitude);
                Log.i("places", photoMetadata.toString());

                // Create a FetchPhotoRequest.
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                    @Override
                    public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();

                        Log.i("places", "Got photo!");
                        Log.i("places", "photoMetadata: " + photoMetadata.toString());

                        photoUrlOfAtt = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&photoreference=" +
                                photoMetadata.a() + "&key=" + API_KEY;
                        longitudeOfAttraction = place.getLatLng().longitude;
                                latitudeOfAttraction = place.getLatLng().latitude;

                        att_image.setImageBitmap(bitmap);
                        att_tv.setText(place.getName());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            Log.e("places", "Place not found: " + exception.getMessage());
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e("places", "Place not found: " + exception.getMessage());
                }
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    public void registerItemClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.register_dialog, null);
        final EditText username_et = dialogView.findViewById(R.id.register_dialog_username_et);
        final EditText password_et = dialogView.findViewById(R.id.register_dialog_password_et);
        final EditText fullname_et = dialogView.findViewById(R.id.register_dialog_fullname_et);
        Button submit_btn = dialogView.findViewById(R.id.register_dialog_submit_btn);
      final  RadioGroup radioGroup = dialogView.findViewById(R.id.register_dialog_radio);
      final RadioButton radioButtonMale = dialogView.findViewById(R.id.register_dialog_male);
        final RadioButton radioButtonFemale = dialogView.findViewById(R.id.register_dialog_female);
      final  NumberPicker numberPicker = dialogView.findViewById(R.id.register_dialog_number_picker);
      final  EditText country_et = dialogView.findViewById(R.id.register_dialog_country_et);


        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                numPickerValue = newVal;
            }
        });

        numberPicker.setMaxValue(99);
        numberPicker.setMinValue(0);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = username_et.getText().toString();
                String password = password_et.getText().toString();

                final String fullname = fullname_et.getText().toString();
                String genderStr;

                // Gender:
                if( !radioButtonMale.isChecked() && !radioButtonFemale.isChecked() ) genderStr = s(R.string.other);
                else {
                        if(radioButtonMale.isChecked()) genderStr = s(R.string.male);
                        else genderStr = s(R.string.female);
                }
                final String finalGender = genderStr;

                firebaseAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            firebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fullname).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("name").setValue(fullname);
                                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("age").setValue(numPickerValue);
                                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("gender").setValue(finalGender);
                                        usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("country").setValue(country_et.getText().toString());

                                        Toast.makeText(MainActivity.this,  s(R.string.welcome)+" " + fullname, Toast.LENGTH_SHORT).show();

                                    } else {
                                        FirebaseAuthException e = (FirebaseAuthException)task.getException();
                                        if (e!=null)
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Toast.makeText(MainActivity.this, s(R.string.registered), Toast.LENGTH_SHORT).show();
                            usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(localToken);
                            dialog.dismiss();
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException)task.getException();
                            if (e!=null)
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            getLoc();
            }
        }


    public void friendItemClicked() {
        fragmentManager = getSupportFragmentManager();
        //create fragment transaction
        fragmentTransaction = fragmentManager.beginTransaction();
        FriendFragment friendFragment = new FriendFragment();
        fragmentTransaction.replace(R.id.recycler_container, friendFragment);
        fragmentTransaction.addToBackStack("friendFragment");
        fragmentTransaction.commit();
        friendFragment.showFrinds(firebaseAuth.getCurrentUser().getUid());
    }

    public void profileItemClicked() {
        fragmentManager = getSupportFragmentManager();
        //create fragment transaction
        fragmentTransaction = fragmentManager.beginTransaction();
        ProfileFragment fragProfile = new ProfileFragment();
        fragmentTransaction.replace(R.id.recycler_container, fragProfile);
        fragmentTransaction.addToBackStack("fragProfile");
        fragmentTransaction.commit();
    }

    public void feedItemClicked() {

        actionBar.setTitle("WeTrip");

        navigationView.getMenu().findItem(R.id.item_feed).setChecked(true);

        fragmentManager = getSupportFragmentManager();
        //create fragment transaction
        fragmentTransaction = fragmentManager.beginTransaction();
        FeedFragment feedFragment = new FeedFragment();
        fragmentTransaction.replace(R.id.recycler_container, feedFragment);
        //fragmentTransaction.addToBackStack("feedFragment");
        fragmentTransaction.commit();

    }

    public void myTripsItemClicked() {

        fragmentManager = getSupportFragmentManager();
        //create fragment transaction
        fragmentTransaction = fragmentManager.beginTransaction();
        MyTripsFragment myTripsFragment = new MyTripsFragment();
        fragmentTransaction.replace(R.id.recycler_container, myTripsFragment);
        fragmentTransaction.addToBackStack("myTripsFragment");
        fragmentTransaction.commit();


    }


    public void loginItemClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.login_dialog, null);
        final EditText username_et = dialogView.findViewById(R.id.login_dialog_username_et);
        final EditText password_et = dialogView.findViewById(R.id.login_dialog_password_et);
        Button submit_btn = dialogView.findViewById(R.id.login_dialog_submit_btn);

        View signUp = dialogView.findViewById(R.id.register_login_dialog);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerItemClicked();
                dialog.dismiss();
                navigationView.setCheckedItem(R.id.item_register);
            }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = username_et.getText().toString();
                String password = password_et.getText().toString();

                firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, s(R.string.login_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            usersTable.child(firebaseAuth.getCurrentUser().getUid()).child("token").setValue(localToken);
                        } else {
                            FirebaseAuthException e = (FirebaseAuthException)task.getException();
                            if (e!=null)
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {

        if (navigationView.getMenu().findItem(R.id.item_feed).isChecked())
        {
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(s(R.string.leave_we_trip))
                    .setPositiveButton(s(R.string.ok), dialogClickListener)
                    .setNegativeButton(s(R.string.cancel) , dialogClickListener)
                    .show();
        }
        else feedItemClicked();

    }

    static public boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    private String s(int id)
    {
        String str = new String();
        str = this.getResources().getString(id);
        return str;
    }

    public static void popUp(Context context, String message)
    {
        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(context);
        builder1.setMessage(message);
        builder1.setCancelable(true);

        builder1.setNegativeButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}

