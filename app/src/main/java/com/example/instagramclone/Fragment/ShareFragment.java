package com.example.instagramclone.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.ShareActivity;
import com.example.instagramclone.Adapter.ShareAdapter;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ShareFragment extends Fragment {
    ImageView share_photo ;
    TextView  share_input ;
    EditText share_description  ;
    RecyclerView recycleView ;
    List<User> mUsers ;
    ShareAdapter shareAdapter ;


    FirebaseUser firebaseUser  = FirebaseAuth.getInstance().getCurrentUser();
    String fuser  = firebaseUser.getUid() ;

    String share_image ;
    String postimage , postid ,message  ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_share, container, false);

        mUsers = new ArrayList<>() ;
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE); //lenh hien thi giao dien nay tu cau lenh chuyen giao dien tu mot giao dien khac
        share_image = prefs.getString("profileid", "none"); //hien thi profile tuong ung voi gia tri id duoc nhan trong qua trinh chuyen giao dien


        share_description = view.findViewById(R.id.inputter) ;
        share_photo = view.findViewById(R.id.share_photo) ;
        recycleView  =view.findViewById(R.id.recycler_view_share) ;
        share_input  =view.findViewById(R.id.share_input) ;
        recycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()) ;
        recycleView.setLayoutManager(linearLayoutManager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(share_image);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class) ;
                Glide.with(getContext()).load(post.getPostimage()).into(share_photo);
                postimage = post.getPostimage() ;
                postid = post.getPostid() ;

                System.out.println("A  :  " + postimage);
                System.out.println("B  :  " + postid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;

        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Unsent").child(fuser);

        share_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HashMap<String ,Object> hashMap = new HashMap<>() ;
                message = share_description.getText().toString() ;
                hashMap.put("content" ,message) ;

                ref.setValue(hashMap) ;
            }

            @Override
            public void afterTextChanged(Editable s) {
                HashMap<String ,Object> hashMap = new HashMap<>() ;
                message = share_description.getText().toString() ;
                hashMap.put("content" ,message) ;

                ref.updateChildren(hashMap) ;

            }
        });

        share_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUsers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readUsers();
        return view;


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

                recycleView.setAdapter(shareAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readUsers() {


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users") ;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getId().equals(fuser)){
                        mUsers.add(user);
                    }
                }
                shareAdapter = new ShareAdapter(getContext() , mUsers , postid ,true) ;
                recycleView.setAdapter(shareAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}