package com.shahardror.wetrip;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{

    List<ChatMessage> chatList;
    ChatListener listener;
    Context context;
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

    public ChatAdapter(List<ChatMessage> chatList) { this.chatList = chatList; }

    public interface ChatListener { void onChatClick(int position, View view); }

    public void setListener(ChatListener chatListener) { this.listener = chatListener; }

    public  class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView chatText;
        LinearLayout chatLinearLayout, chatParentLayout;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatText = itemView.findViewById(R.id.chat_card_text);
            chatLinearLayout = itemView.findViewById(R.id.chat_card_linearlayout);
            chatParentLayout = itemView.findViewById(R.id.chat_card_parentlayout);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onChatClick(getAdapterPosition() , v);
                }
            });

        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card,parent,false);

//        view.findViewById(R.id.chat_card_linearlayout).setBackgroundColor(view.getResources().getColor(R.color.colorPrimary));
//
//        TextView tv = view.findViewById(R.id.chat_card_text);
//        tv.setTextColor(Color.WHITE);

        ChatViewHolder holder = new ChatViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = chatList.get(position);
        holder.chatText.setText(msg.getMessage());


//        Log.i("msgComplete","Message: "+msg.getMessage());
//        Log.i("msgComplete","Sender: "+msg.getSender());
//        Log.i("msgComplete","Receiver: "+msg.getReceiver());
//        Log.i("msgComplete","Time: "+msg.getTime_sent());

        Log.i("msg", "current user: "+name+". sender: "+msg.getSender()+". rec: "+msg.getReceiver());

        // if sent by local user, do:
        if (msg.getSender().equals(name))
        {
            //Log.i("msg", "sent by me ("+msg.getSender()+")");
            holder.chatText.setTextColor(Color.WHITE);
            holder.chatLinearLayout.setBackgroundColor(holder.chatLinearLayout.getResources().getColor(R.color.colorPrimary));
            holder.chatParentLayout.setGravity(Gravity.END);
        }
        else
        {
            //Log.i("msg", "sent by remote user ("+msg.getSender()+")");
            holder.chatText.setTextColor(Color.BLACK);
            holder.chatLinearLayout.setBackgroundColor(Color.WHITE);
            holder.chatParentLayout.setGravity(Gravity.START);
        }


    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }
}
