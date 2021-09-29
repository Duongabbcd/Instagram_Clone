package com.example.instagramclone.Adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SearchMsgAdapter extends RecyclerView.Adapter<SearchMsgAdapter.ViewHolder> {
    private Context context ;
    private List<Chat>  mChats ;

    public SearchMsgAdapter(Context context, List<Chat> mChats) {
        this.context = context;
        this.mChats = mChats;
    }

    @NonNull
    @Override
    public SearchMsgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatter ,parent,false) ;
        return new SearchMsgAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchMsgAdapter.ViewHolder holder, int position) {
          Chat chat  = mChats.get(position) ;

          holder.last_msg.setText(chat.getMessage());
          holder.chatter_username.setVisibility(View.VISIBLE);
          holder.chatter_avatar.setVisibility(View.VISIBLE);

          if(chat.getMessage().equals("")){
              holder.last_msg.setText("have sent a link ");
          }
          DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(chat.getSender()) ;
          ref.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  User user = snapshot.getValue(User.class) ;
                  Glide.with(context.getApplicationContext()).load(user.getImageurl()).into(holder.chatter_image)  ;
                  holder.chatter_name.setText(user.getUsername());
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });

        DatabaseReference refer = FirebaseDatabase.getInstance().getReference("Users").child(chat.getReceiver()) ;
        refer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class) ;
                Glide.with(context.getApplicationContext()).load(user.getImageurl()).into(holder.chatter_avatar)  ;
                holder.chatter_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView chatter_name ,last_msg ,msg_date ,chatter_username;
        public ImageView chatter_image , chatter_avatar ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatter_image = itemView.findViewById(R.id.chatter_image);
            chatter_name = itemView.findViewById(R.id.chatter_name);
            last_msg = itemView.findViewById(R.id.last_msg);
            msg_date = itemView.findViewById(R.id.msg_date);
           chatter_username = itemView.findViewById(R.id.chatter_username) ;
           chatter_avatar = itemView.findViewById(R.id.chatter_avatar) ;
        }
    }


}
