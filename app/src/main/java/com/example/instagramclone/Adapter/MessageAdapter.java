package com.example.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.ForwardActivity;
import com.example.instagramclone.Activity.MainActivity;
import com.example.instagramclone.Activity.MessageActivity;
import com.example.instagramclone.Activity.PostDetailActivity;
import com.example.instagramclone.Activity.ProfileActivity;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context mContext ;
    private List<Chat> mChat ;
    private String imageurl ;
    private String desc  ,link;
    private String profileid ;

    public static final int MSG_TYPE_LEFT =0 ;
    public static final int MSG_TYPE_RIGHT = 1 ;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }
    FirebaseUser fuser ;

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right ,parent  , false );
            return  new MessageAdapter.ViewHolder(view) ;
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left ,parent  , false );
            return  new MessageAdapter.ViewHolder(view) ;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
            Chat chat = mChat.get(position) ;
        holder.show_message.setText(chat.getMessage());

            System.out.println("What you are looking for " + imageurl);
        holder.msg_date.setText(chat.getStrDate());

        holder.relative_item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.msg_date.setVisibility(View.VISIBLE);
                holder.layout_show.setVisibility(View.VISIBLE);
                if(!chat.getMessage().equals("this message was deleted !")){
                }
                return false;
            }
        });


        holder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), ForwardActivity.class) ;
                intent.putExtra("content" , chat.getMessage()) ;
                intent.putExtra("postid" , chat.getLink()) ;
                mContext.startActivity(intent);
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMessage(chat.getMessageid());
            }
        });

        holder.dissapear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.layout_show.setVisibility(View.GONE);
            }
        });

           Glide.with(mContext).load(imageurl).into(holder.profile_image) ;
           if(chat.getMessage().equals("have sent a photo")){
               holder.img_message.setVisibility(View.VISIBLE);
               holder.show_message.setVisibility(View.GONE);
               Glide.with(mContext).load(chat.getPhotoUrl()).into(holder.img_message) ;
           }

           if(chat.getMessage().equals("this message was deleted !") ){
               holder.show_message.setText(chat.getMessage());
               holder.show_message.setVisibility(View.VISIBLE);
               holder.img_message.setVisibility(View.GONE);
               holder.share_desc.setVisibility(View.GONE);
               holder.layout_show.setVisibility(View.INVISIBLE);
           }

           holder.show_message.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   holder.msg_date.setVisibility(View.GONE);
                   holder.layout_show.setVisibility(View.GONE);
               }
           });

           holder.img_message.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   holder.msg_date.setVisibility(View.VISIBLE);
                 if(!chat.getMessage().equals("this message was deleted !")){
                     holder.layout_show.setVisibility(View.VISIBLE);
                 }
                   return false;
               }
           });

        holder.img_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.msg_date.setVisibility(View.GONE);
                holder.layout_show.setVisibility(View.GONE);
            }
        });

        holder.share_latyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.msg_date.setVisibility(View.GONE);
                holder.layout_show.setVisibility(View.GONE);
            }
        });
           desc= chat.getMessage() ;
          link = chat.getLink() ;

               holder.share_latyout.setVisibility(View.GONE);
               holder.show_message.setVisibility(View.VISIBLE);
               holder.share_desc.setVisibility(View.GONE);


           if(!link.equals("") ){
               holder.share_latyout.setVisibility(View.VISIBLE);
               holder.show_message.setVisibility(View.GONE);
               if(desc.length() != 0 ){
                   holder.share_desc.setVisibility(View.VISIBLE);
                   holder.share_desc.setText(desc);
                   System.out.println("Length of Desc : " + desc.length());
               }

               DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(link);
               ref.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       Post post  = snapshot.getValue(Post.class);
                       Glide.with(mContext).load(post.getPostimage()).into(holder.msg_image) ;
                       holder.msg_desc.setText(post.getDescription());
                       profileid = post.getPublisher() ;

                       DatabaseReference refer = FirebaseDatabase.getInstance().getReference("Users").child(post.getPublisher());
                       refer.addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                              User user = snapshot.getValue(User.class) ;
                              holder.msg_username.setText(user.getUsername());
                            Glide.with(mContext.getApplicationContext()).load(user.getImageurl()).into(holder.msg_avatar);
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {

                           }
                       });
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
           }

        if(position == mChat.size() -1 ){
            if(chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            }else{
                holder.txt_seen.setText("Delivered");
            }
        }else{
            holder.txt_seen.setVisibility(View.GONE);
        }


        holder.msg_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid",profileid);
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
        });

        holder.msg_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid",profileid);
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
        });

        holder.msg_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), PostDetailActivity.class);
                intent.putExtra("postid" ,link ) ;
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        holder.msg_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), PostDetailActivity.class);
                intent.putExtra("postid" ,link ) ;
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder {

        public TextView show_message  ,msg_date  ,share_desc;
        public ImageView profile_image ,img_message;
        public TextView txt_seen ;
        public RelativeLayout relative_item ;
        public RelativeLayout share_latyout ;
        public CircleImageView msg_avatar ;
        public ImageView msg_image  ,remove ,forward ,dissapear;
        public TextView msg_desc ,msg_username ;
        public LinearLayout layout_show ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message) ;
            profile_image = itemView.findViewById(R.id.profile_image) ;
            txt_seen = itemView.findViewById(R.id.txt_seen) ;
            msg_date =itemView.findViewById(R.id.msg_date);
            img_message = itemView.findViewById(R.id.img_message) ;
            relative_item =itemView.findViewById(R.id.relative_item) ;
            share_latyout = itemView.findViewById(R.id.share_layout) ;
            msg_avatar = itemView.findViewById(R.id.msg_avatar) ;
            msg_image = itemView.findViewById(R.id.msg_image) ;
            msg_desc = itemView.findViewById(R.id.msg_desc) ;
            msg_username = itemView.findViewById(R.id.msg_username)  ;
            share_desc = itemView.findViewById(R.id.share_desc);
            remove = itemView.findViewById(R.id.remove) ;
            forward = itemView.findViewById(R.id.forward) ;
            dissapear = itemView.findViewById(R.id.dissapear) ;
            layout_show = itemView.findViewById(R.id.layout_show) ;
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser() ;
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return  MSG_TYPE_RIGHT ;
        }else{
            return  MSG_TYPE_LEFT ;
        }
    }


    public void deleteMessage(String  content){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(content) ;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("message", "this message was deleted !");
                hashMap.put("link" ,"");

                FirebaseDatabase.getInstance().getReference("Chats")
                        .child(content).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })  ;
    }
}
