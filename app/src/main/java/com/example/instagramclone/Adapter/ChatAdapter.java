package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.MessageActivity;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Support;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context mContext ;
    private List<User> mUsers ;
    private boolean isChat ;
    Support support ;

    public ChatAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    String theLastMessage ;

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatter , parent ,false) ;
      return  new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position) ;

        holder.chatter_name.setText(user.getUsername());

        Glide.with(mContext.getApplicationContext()).load(user.getImageurl()).into(holder.chatter_image) ;

        if(isChat){
            lastMessage(user.getId() , holder.last_msg , holder.msg_date , holder.notseen) ;
        }else{
            holder.last_msg.setVisibility(View.GONE);
        }

        if(isChat){
            if(user.getStatus().equals("online") || user.getStatus().equals("typing")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else{
            holder.chatter_fullname.setVisibility(View.VISIBLE);
            holder.chatter_fullname.setText(user.getFullname());
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), MessageActivity.class) ;
                intent.putExtra("userid" , user.getId()) ;
                intent.putExtra("token" , user.getToken() );
                intent.putExtra("name", user.getUsername());
                mContext.startActivity(intent);
            }
        });
    }

    private void lastMessage(String userid, TextView last_msg, TextView msg_date, ImageView notseen) {
        theLastMessage= "No message" ;
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        String fuser =firebaseUser.getUid() ;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    if(firebaseUser!= null) {
                        if(chat.getReceiver().equals(fuser)  && chat.getSender().equals(userid)  ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(fuser)){

                             if(!chat.getLink().equals("") && chat.getSender().equals(fuser)){
                                theLastMessage = "You have sent a link" ;
                            }else if(!chat.getLink().equals("") && !chat.getSender().equals(fuser)){
                                theLastMessage = "have sent a link" ;
                            }  else if(chat.getSender().equals(fuser)){
                                theLastMessage  ="You : "+ chat.getMessage()  ;
                            }else if(!chat.getSender().equals(fuser)){
                                theLastMessage = chat.getMessage() ;
                            }


                            String strDate =support.calculateTimeAgo(chat.getStrDate()) ;
                            msg_date.setText(strDate);

                            if(!chat.isIsseen() && firebaseUser.getUid().equals(chat.getReceiver())){
                                last_msg.setTypeface(null , Typeface.BOLD);
                                last_msg.setTextColor(ContextCompat.getColor(mContext,R.color.notSeen));

                                notseen.setVisibility(View.VISIBLE);
                            }
                            else{
                                last_msg.setTypeface(null ,Typeface.NORMAL);
                                notseen.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                switch (theLastMessage){
                    case "No message":
                        last_msg.setText("No message");
                    default:
                        last_msg.setText(theLastMessage) ;
                        break ;
                }
                theLastMessage = "No message" ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView chatter_name ,last_msg ,msg_date ,chatter_fullname;
        public ImageView chatter_image , notseen ,img_on , img_off ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notseen = itemView.findViewById(R.id.notseen) ;
            chatter_image = itemView.findViewById(R.id.chatter_image);
            chatter_name = itemView.findViewById(R.id.chatter_name);
            last_msg = itemView.findViewById(R.id.last_msg);
            msg_date = itemView.findViewById(R.id.msg_date);
            img_off = itemView.findViewById(R.id.img_off);
            img_on = itemView.findViewById(R.id.img_on);
            chatter_fullname = itemView.findViewById(R.id.chatter_fullname);
        }

    }


}
