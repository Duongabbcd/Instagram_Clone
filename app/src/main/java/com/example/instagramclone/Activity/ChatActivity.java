package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instagramclone.Adapter.ChatAdapter;
import com.example.instagramclone.Adapter.SearchMsgAdapter;
import com.example.instagramclone.Fragment.AccountFragment;
import com.example.instagramclone.Fragment.ChatFragment;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    ImageView west  ;
    TextView currrentUser,backer ;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String fuser = firebaseUser.getUid() ;

    DatabaseReference reference ;
    TextView search_chatter ;
    private List<User> mUsers ;
    private List<Chat> mChats ;
    FrameLayout frame_message ;
   AppBarLayout begin_appbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        west = findViewById(R.id.west);
        currrentUser = findViewById(R.id.currentUser) ;
        search_chatter =findViewById(R.id.search_chatter) ;
        backer = findViewById(R.id.backer) ;
        frame_message = findViewById(R.id.frame_message) ;
        begin_appbar = findViewById(R.id.begin_appbar);
        mUsers = new ArrayList<>();
        mChats = new ArrayList<>();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_message,
                new ChatFragment()).commit();


        west.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        backer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        userInfo();

        currrentUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileid", firebaseUser.getUid());
                editor.apply();

                Intent intent = new Intent(ChatActivity.this,ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        search_chatter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_message,
                        new AccountFragment()).commit();
                begin_appbar.setVisibility(View.GONE);
            }
        });



    }


    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser) ;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                currrentUser.setText(user.getUsername());

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

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}