package com.shahardror.wetrip;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FriendFragment extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference friendsTable = database.getReference("friends");
    DatabaseReference usersTable = database.getReference("users");
    ArrayList<Friend> friends = new ArrayList<>();
    RecyclerView recyclerView;
    FriendAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //   return super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.activity_feed, container, false);
        recyclerView = root.findViewById(R.id.recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        return root;
    }

    public void showFrinds(String currentUserId) {

        friendsTable.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friends.clear();
                for (DataSnapshot dst : dataSnapshot.getChildren()) {

                    final Friend friend = new Friend("ss", dst.getValue().toString());
                    friends.add(friend);

                    usersTable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                for (int i = 0; i < friends.size(); i++) {
                                    if (friends.get(i).getImageUrl().equals(ds.getKey())) {
                                        friends.get(i).setName(ds.child("name").getValue().toString());
                                        Log.i("tag" , "works: " + ds.child("name").getValue());
                                    }
                                }
                            }
                            adapter = new FriendAdapter(friends);
                            recyclerView.setAdapter(adapter);
                            adapter.setListener(new FriendAdapter.FriendsListener() {
                                @Override
                                public void onFriendClick(int position, View view) {

                                    final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                                    FragmentTransaction fragmentTransaction;
                                    //create fragment transaction
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    OtherProfileFragment otherProfileFragment = new OtherProfileFragment();
                                    fragmentTransaction.replace(R.id.recycler_container, otherProfileFragment);
                                    fragmentTransaction.commit();
                                    fragmentTransaction.addToBackStack("otherProfileFragment");
                                    otherProfileFragment.downloadProfileImage(friends.get(position).imageUrl);
                                    //MainActivity.safeToast.show(getContext(),""+position);
                                }
                            });
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

    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.floating_button).setVisibility(View.GONE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.GONE);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.item_friend);
        MainActivity.actionBar.setTitle(getResources().getString(R.string.my_friends));
    }
}
