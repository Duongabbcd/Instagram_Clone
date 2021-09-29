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
import android.widget.TextView;

import com.example.instagramclone.Adapter.ShareAdapter;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardActivity extends AppCompatActivity {
    DatabaseReference reference ;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String content ,postid ;
    SocialAutoCompleteTextView forward_input ;
    RecyclerView recyclerView ;
    List<User> mUsers ;
    ShareAdapter shareAdapter ;
    String fuser  = firebaseUser.getUid() ;
    TextView done ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forward);

        Intent intent = getIntent() ;
        content = intent.getStringExtra("content");
        postid = intent.getStringExtra("postid") ;
        System.out.println("String Extra :" + content);
        mUsers = new ArrayList<>();

        forward_input = findViewById(R.id.forward_input) ;
        recyclerView = findViewById(R.id.recycler_view_sending) ;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        forward_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUsers(s.toString()) ;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        readUsers();
    }
    private void searchUsers (String s) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("username").startAt(s).endAt(s + "\uf8ff"); //lenh get data tu table Users voi dieu kien username = String s gom co ky tu dau tien StartAt cho den ky tu ket thuc EndAt

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //doc duoc data tu Instance trong Firebase
                mUsers.clear(); //thiet lap list mUsers clear
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //vong lap for doc toan bo data trong table
                    User user = snapshot.getValue(User.class); //get toan bo gia tri cua thuoc tinh cua 1 User
                    mUsers.add(user); //add User vao list mUsers
                }

                recyclerView.setAdapter(shareAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readUsers() {
      DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users") ;
      reference.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              mUsers.clear();
              for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                  User user =   dataSnapshot.getValue(User.class) ;
                  if(!user.getId().equals(fuser)){
                      mUsers.add(user) ;

                  }
              }
              System.out.println("Your user list is :" + mUsers.size() );

              shareAdapter = new ShareAdapter(ForwardActivity.this , mUsers , postid ,false);
             recyclerView.setAdapter(shareAdapter);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {

          }
      }) ;

    }


    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser);
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("status" , status) ;
        reference.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online") ;
    }
}