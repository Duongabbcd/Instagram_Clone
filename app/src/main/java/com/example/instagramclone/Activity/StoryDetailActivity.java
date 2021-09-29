package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Model.Story;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryDetailActivity extends AppCompatActivity {
  FirebaseUser  firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
  DatabaseReference reference  ;
    ImageView old_image ,exit ;
    TextView story_owner , old_date ;
    CircleImageView avatar ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        old_image=  findViewById(R.id.old_image) ;
        avatar = findViewById(R.id.avatar) ;
        story_owner = findViewById(R.id.story_owner);
        old_date= findViewById(R.id.old_date) ;
        exit = findViewById(R.id.exit)  ;

        String fuser =   FirebaseAuth.getInstance().getCurrentUser().getUid() ;

        Intent intent = getIntent(); //ham nhan gia tri tu lenh Intent cua giao dien cu
        String storyid = intent.getStringExtra("oldstoryid"); //hien thi giao dien post tuong ung voi post id duoc nhan khi thuc hien lenh chuyen giao dien

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(fuser).child(storyid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Story story = snapshot.getValue(Story.class);

                old_date.setText(story.getStrDate());
                Glide.with(getApplicationContext()).load(story.getImageurl()).into(old_image) ;
                getUserInfo(fuser) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getUserInfo(String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                story_owner.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(avatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("status" , status) ;
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        support.status("offline");
//    }
}