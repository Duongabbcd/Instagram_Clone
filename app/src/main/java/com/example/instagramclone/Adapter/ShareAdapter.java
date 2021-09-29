package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Fragment.ProfileFragment;
import com.example.instagramclone.Fragment.ShareFragment;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.Unsent;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {
    private Context mContext ;
    private List<User> users ;
    String postid , content ,postimage ;
    String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid() ;
    boolean isSend ;


    public ShareAdapter(Context mContext, List<User> users, String postid, boolean isSend) {
        this.mContext = mContext;
        this.users = users;
        this.postid = postid;
        this.isSend = isSend;
    }

    @NonNull
    @Override
    public ShareAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_share ,parent,false ) ;
        return new ShareAdapter.ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ShareAdapter.ViewHolder holder, int position) {
            User user = users.get(position) ;

        Glide.with(mContext).load(user.getImageurl()).into(holder.share_avatar)  ;

        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post  =  snapshot.getValue(Post.class) ;
                postimage = post.getPostimage() ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Unsent").child(fuser);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Unsent unsent = snapshot.getValue(Unsent.class) ;
                        if(dataSnapshot.exists()){
                            content = unsent.getContent() ;
                        }

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;

            holder.share_username.setText(user.getUsername());
            holder.share_fullname.setText(user.getFullname());
           if(isSend){
               holder.btnShare.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       sendMessage(user.getId() , content ,postimage);
                   }
               });
           }else{
               holder.btnShare.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       sendMessage(user.getId() , content ,postimage);
                   }
               });
           }


    }


    @Override
    public int getItemCount() {
       return  users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView share_avatar ;
        TextView share_fullname ,share_username ;
        Button btnShare ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            share_avatar = itemView.findViewById(R.id.share_avatar) ;
            share_fullname = itemView.findViewById(R.id.share_fullname) ;
            share_username = itemView.findViewById(R.id.share_username) ;
            btnShare = itemView.findViewById(R.id.btnShare) ;
        }
    }

    private void sendMessage(String userid ,String message ,String postimage) {
        Date date  = new Date() ;
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
        String strDate = format.format(date) ;

        DatabaseReference reference  =  FirebaseDatabase.getInstance().getReference() ;
        String messageid =  reference.push().getKey();
        HashMap<String ,Object> hashMap = new HashMap<>() ;
        hashMap.put("feeling" , -1)  ;
        hashMap.put("sender" ,fuser) ;
        hashMap.put("receiver" ,userid) ;
        hashMap.put("message" ,message) ;
        hashMap.put("strDate" ,strDate) ;
        hashMap.put("isseen" ,false) ;
        hashMap.put("photoUrl" ,postimage);
        hashMap.put("link",postid);
        hashMap.put("messageid" , messageid) ;

        Toast.makeText(mContext , "Successfully !" ,Toast.LENGTH_SHORT).show();

        reference.child("Chats").child(messageid).setValue(hashMap) ;

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatlist")
                .child(fuser)
                .child(userid) ;
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid) ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRefReceiver =  FirebaseDatabase.getInstance().getReference("chatlist")
                .child(userid)
                .child(fuser) ;
        chatRefReceiver.child("id").setValue(fuser) ;

    }
}
