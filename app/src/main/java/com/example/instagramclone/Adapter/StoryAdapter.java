package com.example.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.instagramclone.Activity.AddStoryActivity;
import com.example.instagramclone.Model.Story;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Activity.StoryActivity;

import java.util.List;
public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder>{

    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    // 1 Story gom co Id ,userid , imageurl ,timestart ,timeend

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == 0) { //neu chua co story nao thi :
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_add_story, viewGroup, false);
            return new StoryAdapter.ViewHolder(view);

        } else {//neu da co tu 1 story tro len thi :
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_story, viewGroup, false);
            return new StoryAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Story story = mStory.get(i); //the hien gia tri cua moi mot the la co dinh ,voi danh sach cac object story trong list va vi tri tuong ung trong recycleview

        userInfo(viewHolder, story.getUserid(), i);

        if (viewHolder.getAdapterPosition() != 0) {//neu recycle view co tren 1 Object Story thi hien len
            seenStory(viewHolder, story.getUserid());
        }

        if (viewHolder.getAdapterPosition() == 0){//neu khong co story thi thuc hien ham myStory
            myStory(viewHolder.addstory_text, viewHolder.story_plus, false);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() { //doi voi moi Object story co tren RecycleView
            @Override
            public void onClick(View view) {
                if (viewHolder.getAdapterPosition() == 0){
                    myStory(viewHolder.addstory_text, viewHolder.story_photo, true);
                } else {
                    // Truyen du lieu den StoryActivity
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView story_photo, story_plus, story_photo_seen;
        public TextView story_username, addstory_text ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_username = itemView.findViewById(R.id.story_username);
            story_plus = itemView.findViewById(R.id.story_plus);
            addstory_text = itemView.findViewById(R.id.addstory_text);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        return 1;
    }

    //Ham nay dung de the hien trang thai avatar tuong duong nut mo xem Story cua moi user co trong he thong
    private void userInfo(final ViewHolder viewHolder, String userid, final int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
           if(user.getImageurl() != null){ //neu User da co avatar thi hien avatar loai co vien mau xam
                Glide.with(mContext.getApplicationContext()).load(user.getImageurl()).into(viewHolder.story_photo);
            }

                if (pos != 0) { //Neu user da dang tu 1 story tro len thi hien vien avatar loai co vien mau do
                    Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Ham nay co chuc nang thuc hien cac hanh dong cua user hien tai doi voi Story cua chinh minh va cua nguoi khac
    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()); // Get User hien tai dang trong phien dang nhap bang Id
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis(); //lay gia tri cua thoi diem hien tai tu he thong ,tinh don vi milisecond
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Story story = snapshot.getValue(Story.class); //dataSnapshot dùng để đọc data từ Instance trong Firebase

                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){ //neu van trong thoi gian 1 story duoc hien thi thi 1 story = 1 count
                        count++;
                    }
                }

                if (click) {//Neu nhan vao avatar tren story
                    if (count > 0) {//Neu da co Story
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //TODO: go to story
                                        Intent intent = new Intent(mContext, StoryActivity.class);
                                        intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid()); //Truyen data Id  cua User den StoryActivity thuc hien lenh xem xet info nguoi da view Story
                                        mContext.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) { //Truyen lenh add story den AddStoryActivity
                                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                                        mContext.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    } else { //Neu chua co Story
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                } else { //Neu khong nhan vao 1 Story bat ky thi :

                    if (count > 0){ //hien thi text My story va dau cong add Story bien mat
                        textView.setText("My story");
                        imageView.setVisibility(View.GONE);

                    } else {//Khi nguoi dung hien tai chua dang 1 story nao thi hien textAddStory
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userid){
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //get data tu Instance trong firebase voi dieu kien neu co nguoi an vao xem ...
                                                                             // ... story truoc khi den thoi diem Story bi an thi tu dong tang len
                    if (!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()){
                        i++;


                    }
                }

                if ( i > 0){ //Neu Story da co View
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);

                } else {//Neu Story khong co View
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}