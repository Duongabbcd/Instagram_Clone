package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.ProfileActivity;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Support;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ImageViewHolder> {
    Support support ;
    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context context, List<Notification> notification){
        mContext = context;
        mNotification = notification;
    }


    //1 Notification co : userid ,text , postid ,boolean ispost
    @NonNull
    @Override
    public NotificationAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationAdapter.ImageViewHolder holder, final int position) {

        final Notification notification = mNotification.get(position);

        holder.text.setText(notification.getText());

        getUserInfo(holder.image_profile, holder.username, notification.getUserid());

        System.out.println("publisher id is : " + notification.getUserid());

        //kiem tra neu Notification duoc tao ra thi :
        if (notification.isIspost()) {
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostid());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }
        String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid() ;
        if(fuser == notification.getUserid()){
            holder.post_image.setVisibility(View.GONE);
            holder.image_profile.setVisibility(View.GONE);
            holder.text.setVisibility(View.GONE);
            holder.not_date.setVisibility(View.GONE);
            holder.username.setVisibility(View.GONE);
        }

        String date = support.calculateTimeAgo(notification.getStrDate()) ;
        holder.not_date.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isIspost()) { //Kiem tra Notification duoc tao ra hay chua

                    //Ham truyen du lieu ve ma bai viet den Fragment : PostDetailFragment
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("postid", notification.getPostid());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                            new PostDetailFragment()).commit();
                } else {

                    //Ham truyen du lieu ve ma nguoi dung den Fragment : ProfileFragment
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", notification.getUserid());
                    editor.apply();

                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });
        if(notification.isIsseen() == false ){
            holder.notif_ssen.setVisibility(View.VISIBLE);
            holder.notif_ssen.animate().alpha(0f).setDuration(2000) ;
        }

    }


    //
    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image  , notif_ssen;
        public TextView username, text ,not_date;

        public ImageViewHolder(View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);
            not_date = itemView.findViewById(R.id.not_date);
            notif_ssen = itemView.findViewById(R.id.notif_seen);
        }
    }

    /*
    hàm lấy thông tin của người tương tác với bài viết của người đăng : publisherid , avatar ,username
     */
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid); //get User by Id

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); //dataSnapshot dùng để đọc data từ Instance trong Firebase
                Glide.with(mContext).load(user.getImageurl()).into(imageView);   // lay gia tri imageUrl cua user
                username.setText(user.getUsername());                           // lay gia tri username cua user


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /*
hàm lấy thông tin bài viết của người đăng : post_image ,postid
 */
    private void getPostImage(final ImageView post_image, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Posts").child(postid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostimage()).into(post_image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}