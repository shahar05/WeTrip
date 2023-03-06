package com.shahardror.wetrip;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendsViewHolder> {

    List<Friend> friendList;
    Context context;
    FriendsListener listener;

    public FriendAdapter(List<Friend> friendList) {
        this.friendList = friendList;
    }
    public interface FriendsListener {
        void onFriendClick(int position, View view);
    }

    public void setListener(FriendsListener tripsListener) {
        this.listener = tripsListener;
    }

    public  class FriendsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView friendImage;
        TextView friendName;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            friendImage = itemView.findViewById(R.id.friend_image);
            friendName = itemView.findViewById(R.id.friend_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFriendClick(getAdapterPosition() , v);
                }
            });
        }
    }


    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card,parent,false);
        FriendsViewHolder holder = new FriendsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        Friend friend = friendList.get(position);

        holder.friendName.setText(friend.getName());
        String imageStr = "https://firebasestorage.googleapis.com/v0/b/wetrip-id.appspot.com/o/users%2F"+friend.getImageUrl() +".jpg?alt=media&token=48fe0e1f-9787-4734-91d0-bd4fcf609249";

        Picasso.with(context).load(imageStr).into(holder.friendImage);

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

}
