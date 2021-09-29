package com.example.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.MainActivity;
import com.example.instagramclone.Model.Comment;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Support;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ImageViewHolder> {
        Support support ;
        private Context mContext;
        private List<Comment> mComment;
        private String postid;

        private FirebaseUser firebaseUser;

        public CommentAdapter(Context context, List<Comment> comments, String postid){
            mContext = context;
            mComment = comments;
            this.postid = postid;
        }
        //1 Comment gồm có : id ,nội dung và người đăng
        @NonNull
        @Override
        public CommentAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
            return new CommentAdapter.ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CommentAdapter.ImageViewHolder holder, final int position) {

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); //Dùng để lấy đối tượng hiện tại (tức là đối tượng đã viết comment)
            final Comment comment = mComment.get(position); //Thể hiện mỗi thẻ trong comment_item này đều nhận một giá trị không đổi


            holder.comment.setText(comment.getComment());
            String timeAgo  =support.calculateTimeAgo( comment.getStrDate()) ;
            holder.com_date.setText(timeAgo);
            getUserInfo(holder.image_profile, holder.username, comment.getPublisher());

            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(mContext, MainActivity.class);  // Truyen du lieu den Activity
                    intent.putExtra("publisherid", comment.getPublisher());
                    mContext.startActivity(intent);
                }
            });

            holder.image_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid", comment.getPublisher());
                    mContext.startActivity(intent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (comment.getPublisher().equals(firebaseUser.getUid())) {
   //Xay dung ham xoa comment bang AlertDialog
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setTitle("Do you want to delete?");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference("Comments")     //   Ham xoa du lieu trong Firebase
                                                .child(postid).child(comment.getCommentid())
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    return true;
                }
            });

        }


    @Override
        public int getItemCount() {
            return mComment.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {

            public ImageView image_profile;
            public TextView username, comment ,com_date;

            public ImageViewHolder(View itemView) {
                super(itemView);

                image_profile = itemView.findViewById(R.id.image_profile);
                username = itemView.findViewById(R.id.username);
                comment = itemView.findViewById(R.id.comment);
                com_date = itemView.findViewById(R.id.com_date);
            }
        }


    /*
hàm lấy thông tin của người tương tác với bài viết của người đăng : publisherid , avatar ,username
 */
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(publisherid); //get User by Id

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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


}
