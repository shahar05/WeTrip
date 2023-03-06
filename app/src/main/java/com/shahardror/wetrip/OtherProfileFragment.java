package com.shahardror.wetrip;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import de.hdodenhof.circleimageview.CircleImageView;

public class OtherProfileFragment extends Fragment {
    AlertDialog bigImageDialog;

    boolean canTakePicture = false;
    final int WRITE_PERMISSION_REQUEST = 1;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("users");
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersTable = database.getReference("users");
    DatabaseReference friendsTable = database.getReference("friends");
    CircleImageView userImage;
    ImageButton addFriendImage, openChatImage , removeFriend , disableMsg;
    TextView ageTv , countryTv,genderTv;
    TextView userNameTextView;
    Bitmap bigImageBitmap;
    String remoteUid, currUserIdStr;
    boolean firstTime = true;
    boolean canRemoveNow = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        View root = inflater.inflate(R.layout.profile_layout, container, false);
        userImage = root.findViewById(R.id.profile_layout_user_image);
        addFriendImage = root.findViewById(R.id.profile_layout_add_friend_btn);
        openChatImage = root.findViewById(R.id.profile_layout_chat_btn);

        userNameTextView = root.findViewById(R.id.profile_layout_user_name);
        ageTv = root.findViewById(R.id.profile_layout_user_age);
        countryTv = root.findViewById(R.id.profile_layout_user_country);
        genderTv = root.findViewById(R.id.profile_layout_user_gender);

        removeFriend = root.findViewById(R.id.profile_layout_remove_friend_btn);
        disableMsg = root.findViewById(R.id.disable_profile_layout_chat_btn);
        currUserIdStr = firebaseAuth.getCurrentUser().getUid();

        friendsTable.child(currUserIdStr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (firstTime){
                    Log.i("tag" , "should called once");
                    firstTime = false;
                    boolean friendExist = false;
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        if (remoteUid.equals(ds.getValue())){
                            friendExist = true;
                            break;
                        }
                    }
                    if(friendExist){
                        canRemoveNow = true;
                        addFriendImage.setVisibility(View.GONE);
                        removeFriend.setVisibility(View.VISIBLE);
                        disableMsg.setVisibility(View.GONE);
                        openChatImage.setVisibility(View.VISIBLE);
                       
                    }else {
                        canRemoveNow = false;
                        disableMsg.setVisibility(View.VISIBLE);
                        openChatImage.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendImage.setVisibility(View.VISIBLE);
                removeFriend.setVisibility(View.GONE);
                disableMsg.setVisibility(View.VISIBLE);
                openChatImage.setVisibility(View.GONE);

                friendsTable.child(currUserIdStr).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(canRemoveNow) {
                                Log.i("tag" , "Removing........ DataBase :(");

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getValue().toString().equals(remoteUid)) {
                                        ds.getRef().removeValue();
                                        break;
                                    }
                                }
                                friendsTable.child(remoteUid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (canRemoveNow) {
                                            canRemoveNow = false;
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                if (ds.getValue().toString().equals(currUserIdStr)) {
                                                    ds.getRef().removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                            }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });



        disableMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getContext() != null){
                    Toast.makeText(getContext(), s(R.string.must_be_friend), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        addFriendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    friendsTable.child(remoteUid).push().setValue(currUserIdStr).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            friendsTable.child(currUserIdStr).push().setValue(remoteUid).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    canRemoveNow = true;
                                }
                            });
                        }
                    });
                MainActivity.safeToast.show(getContext(),s(R.string.add_friend));
                disableMsg.setVisibility(View.GONE);
                openChatImage.setVisibility(View.VISIBLE);
                removeFriend.setVisibility(View.VISIBLE);
                addFriendImage.setVisibility(View.GONE);
            }
        });

        openChatImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction;
                //create fragment transaction
                fragmentTransaction = fragmentManager.beginTransaction();
                ChatFragment chatFragment = new ChatFragment();
                fragmentTransaction.replace(R.id.recycler_container, chatFragment);
                fragmentTransaction.commit();
                fragmentTransaction.addToBackStack("chatFragment");
                chatFragment.showChat(firebaseAuth.getCurrentUser().getUid(), remoteUid);

            }
        });




        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogFunction();
            }
        });

        return root;
    }




    public void openDialogFunction() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.edit_image_layout,null);
        ImageView imageViewProfileImageBig = dialogView.findViewById(R.id.profile_image_edit_layout);
        ImageButton imageButtonBack = dialogView.findViewById(R.id.back_btn_edit_layout);
        ImageButton imageButtonEditImage = dialogView .findViewById(R.id.edit_profile_image_edit_layout);
        builder.setView(dialogView);
        bigImageDialog = builder.create();
        bigImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bigImageDialog.show();



        if(bigImageBitmap != null)
        imageViewProfileImageBig.setImageBitmap(bigImageBitmap);

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigImageDialog.dismiss();
            }
        });
        imageButtonEditImage.setVisibility(View.GONE);

    }


    public void downloadProfileImage( final String otherUserUid){


        firebaseAuth = FirebaseAuth.getInstance();
        currUserIdStr = firebaseAuth.getCurrentUser().getUid();
        remoteUid = otherUserUid;
        Log.i("tag" , "other: "+ remoteUid + "me: "+ currUserIdStr);

        usersTable.child(otherUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                // set Name of The User
                if(dataSnapshot.hasChild("name"))
                    userNameTextView.setText(dataSnapshot.child("name").getValue().toString());

                if (dataSnapshot.hasChild("gender"))
                    genderTv.setText( dataSnapshot.child("gender").getValue().toString());

                if (dataSnapshot.hasChild("age"))
                    ageTv.setText( dataSnapshot.child("age").getValue().toString());

                if (dataSnapshot.hasChild("country"))
                    countryTv.setText( dataSnapshot.child("country").getValue().toString());





                if  ( dataSnapshot.hasChild("photo") ){ // if user have profile Image
                    final DataSnapshot dataSnapshot1 = dataSnapshot.child("photo");
                    String imageStr = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F"+otherUserUid+".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";
                    if(getContext() != null){
                        RequestQueue requestQueueImage = Volley.newRequestQueue(getContext());
                        ImageRequest imageRequest = new ImageRequest(imageStr, new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {

                                if(dataSnapshot1.child("fromExternal").getValue().toString().equals("false")){
                                    response = RotateBitmap(response , 90);
                                }
                                userImage.setImageBitmap(response);
                                bigImageBitmap = response;

                            }
                        }, 2000, 2000, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Toast.makeText(getContext(), "ata ba kivon ahi", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueueImage.add(imageRequest);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        getActivity().findViewById(R.id.floating_button).setVisibility(View.GONE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.GONE);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.item_friend);
        MainActivity.actionBar.setTitle("");
    }
    private String s(int id)
    {
        String str = new String();
        str = this.getResources().getString(id);
        return str;
    }


}
