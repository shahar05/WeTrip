package com.shahardror.wetrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference chatsTable = database.getReference("chats");
    DatabaseReference usersTable = database.getReference("users");
    ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    RecyclerView recyclerView;
    ChatAdapter adapter;
    String chatKey;
    String localUID, remoteUID, localUsername = "localuser", remoteUsername = "remoteuser", remoteToken;

    ImageButton chat_layout_send_btn;
    EditText chat_layout_et;
    LinearLayout chat_layout, chat_toolbar;
    TextView chat_toolbar_tv;
    CircleImageView chat_toolbar_image;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_feed, container, false);
        recyclerView = root.findViewById(R.id.recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        chat_layout_send_btn = getActivity().findViewById(R.id.chat_layout_send_btn);
        chat_layout_et = getActivity().findViewById(R.id.chat_layout_et);
        chat_layout = getActivity().findViewById(R.id.chat_layout);

        chat_toolbar_tv = getActivity().findViewById(R.id.chat_toolbar_tv);
        chat_toolbar_image = getActivity().findViewById(R.id.chat_toolbar_image);


        chat_toolbar = getActivity().findViewById(R.id.chat_toolbar_layout);

        chat_layout_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChatMessage(chat_layout_et.getText().toString());
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.i("chatfuncs","onStart called");
        setLayoutMargins(true,true);
        chat_layout.setVisibility(View.VISIBLE);
        chat_toolbar.setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.floating_button).setVisibility(View.GONE);
        getActivity().findViewById(R.id.refresh_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.find_appropriate_trips_btn_main_activity).setVisibility(View.GONE);

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        navigationView.setCheckedItem(R.id.item_friend);
        if(getContext() != null)
          MainActivity.actionBar.setTitle(  getResources().getString(R.string.chat) );

    }

    @Override
    public void onStop() {
        super.onStop();
        //Log.i("chatfuncs","onStop called");
        setLayoutMargins(true,false);
        chat_layout.setVisibility(View.GONE);
        chat_toolbar.setVisibility(View.GONE);
    }

    public void showChat(String localUserID, final String remoteUserID) {

        localUID = localUserID;
        remoteUID = remoteUserID;

        chatKey = getChatKey(localUID,remoteUID);

        usersTable.child(localUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                localUsername = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersTable.child(remoteUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                remoteUsername = dataSnapshot.child("name").getValue().toString();
                chat_toolbar_tv.setText(remoteUsername);
                String url = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F" +
                        remoteUserID +
                        ".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";

                if (getContext() != null)
                    Glide.with(getContext()).load(url).into(chat_toolbar_image);

                if (dataSnapshot.child("token").getValue() != null)
                    remoteToken = dataSnapshot.child("token").getValue().toString();
                else Log.i("FCM_ChatFragment", "no token for remote user!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatsTable.child(chatKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessages.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    final ChatMessage chatMessage = new ChatMessage(
                            ds.child("message").getValue().toString(),
                            ds.child("sender").getValue().toString(),
                            ds.child("receiver").getValue().toString(),
                            ds.child("time_sent").getValue().toString()
                    );

                    chatMessages.add(chatMessage);

                    adapter = new ChatAdapter(chatMessages);
                    recyclerView.setAdapter(adapter);

                    adapter.setListener(new ChatAdapter.ChatListener() {
                        @Override
                        public void onChatClick(int position, View view) {
                            // do something
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getContext(), "DB ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getChatKey(String u1, String u2) {

        int hash1 = u1.hashCode();
        int hash2 = u2.hashCode();

        long result = hash1 + hash2;
        Long resultLong = result;

        return resultLong.toString();
    }

    public void sendChatMessage(String msg) {

        chat_layout_et.setText(null);

        if (    msg.equals(null) ||
                msg.equals("")   ||
                msg.equals(" "))
        {
            MainActivity.safeToast.show(getContext(),getResources().getString(R.string.msg_empty) );
            return;
        }

        Date currentTime = Calendar.getInstance().getTime();

        ChatMessage chatMessage = new ChatMessage(
                msg,
                localUsername,
                remoteUsername,
                currentTime.toString());

                chatsTable.child(chatKey).push().setValue(chatMessage);
                //Log.i("chat","Chatkey: "+chatKey+", Message: "+msg +" from "+localUsername+ " to "+ remoteUsername);

        // FCM:
        eranSend(msg);

    }

        private void setLayoutMargins(boolean top, boolean bottom)
    {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        RelativeLayout relativeLayout = getActivity().findViewById(R.id.recycler_container);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();

        if (top) params.topMargin = actionBarHeight;
        else params.topMargin = 0;

        if (bottom) params.bottomMargin = actionBarHeight;
        else params.bottomMargin = 0;

    }

    private void eranSend(String textToSend) {

        final JSONObject rootObject  = new JSONObject();
        try {
//            if (groupAcb.isChecked() && groupBcb.isChecked())
//                rootObject.put("condition", "'A' in topics || 'B' in topics");
//            else if(groupAcb.isChecked())
//                rootObject.put("to", "/topics/A");
//            else if(groupBcb.isChecked())
//                rootObject.put("to", "/topics/B");
//            else return;

            Log.i("eranSend", "token: "+MainActivity.localToken);
            rootObject.put("to", remoteToken);

            rootObject.put("data",new JSONObject()
                    .put("message",textToSend)
                    .put("senderName",localUsername)
                    .put("senderUID",localUID));

            String url = "https://fcm.googleapis.com/fcm/send";

            RequestQueue queue = Volley.newRequestQueue(getContext());
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("eranSend", response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.i("eranSend", error.toString());
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<>();
                    headers.put("Content-Type","application/json");
                    headers.put("Authorization","key="+"AAAAeRKYff4:APA91bG45eIYzmqYVnUmzgoGKBK5io4onPHJEJ1REvplUJ0S62c_-5P4OwOFNK8NVqyFNCO79cxs4aOY9pbsHZ3KbZ2I1smervbYoGq4y3AqdK1vVio2QxL8yP8wouJTpS3H5RP4QGOV");
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);
            queue.start();


        }catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
