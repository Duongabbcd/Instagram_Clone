package com.example.instagramclone.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instagramclone.Adapter.TagAdapter;
import com.example.instagramclone.Adapter.UserAdapter;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.android.gms.vision.text.Line;
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
import java.util.List;


public class SearchFragment extends Fragment {
    // Hiện tat ca cac Hashtags va cac Accounts trong he thong

    private RecyclerView recyclerView;
    private List<User> mUsers;
    private UserAdapter userAdapter;

    private RecyclerView recyclerViewTags;
    private List<String> mHashTags; //list hien danh sach Hastags
    private List<String> mHashTagsCount;
    private TagAdapter tagAdapter; //list hien so luong bia viet tuong ung moi Hashtags

    private SocialAutoCompleteTextView search_bar;
    private ImageView finisher ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext() , mUsers , true);


        recyclerViewTags = view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setHasFixedSize(true);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));

        mHashTags = new ArrayList<>();
        mHashTagsCount = new ArrayList<>();
        tagAdapter = new TagAdapter(getContext() , mHashTags , mHashTagsCount);

        search_bar = view.findViewById(R.id.search_bar);
        finisher = view.findViewById(R.id.finisher);

        readUsers();
        readTags();

        search_bar.addTextChangedListener(new TextWatcher() { //thiet lap search bar de thuc hien chuc nang search
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString()); //thuc hien tim User co username giong nhat co the voi String dang duoc nhap
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            } //thuc hien search hashtag tuong ung voi gis tri String da duoc nhap xong
        });

        finisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUsers();
                readTags();
            }
        });

        return view;
    }


    private void readUsers() {
    //ham hien thi tat ca account trong he thong
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        //get all data tu table Users
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(search_bar.getText().toString())){ //doc duoc data tu Instance trong Firebase
                    mUsers.clear(); //thiet lap list mUsers clear
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){//vong lap for doc toan bo data trong table
                        User user = snapshot.getValue(User.class); //get toan bo gia tri cua thuoc tinh cua 1 User
                        mUsers.add(user); //add User vao list mUsers
                    }


                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void searchUser (String s) {
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

                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readTags() { //doc hashtags

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            //get all HashTags tu database
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //doc duoc data tu Instance trong Firebase
                mHashTags.clear(); //set list mHashtags  clear
                mHashTagsCount.clear();//set list mHashtagsCount clear

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //vong lap for doc toan bo data trong table
                    mHashTags.add(snapshot.getKey()); //add vao list ten cua Hashtag hay chinh la id cua Hashtags
                    mHashTagsCount.add(snapshot.getChildrenCount() + ""); //add vao list gom tong so bai viet tuong ung voi moi gia tri mHastags
                }


                recyclerViewTags.setAdapter(tagAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void filter (String text) {
        //ham doc va dem tong so hashtags voi moi ten cua tag
        List<String> mSearchTags = new ArrayList<>();
        List<String> mSearchTagsCount = new ArrayList<>();

        for (String s : mHashTags) { //vong lap for chay het gia tri cua mHashtags
            if (s.toLowerCase().contains(text.toLowerCase())){ //set ten Hashtags dang chu thuong
                mSearchTags.add(s); //add vao list mSearchTags tat ca ten cua Hashtags tuong duong voi String s
                mSearchTagsCount.add(mHashTagsCount.get(mHashTags.indexOf(s))); //add vao list tong so bai viet co Hashtags tuong duong voi String s
            }
        } //indexOf tim vi tri -chi muc cua mot chuoi con cho truoc trong 1 chuoi cha xac dinh

        tagAdapter.filter(mSearchTags , mSearchTagsCount); //hien len tagAdapter gom co ten cua Hashtags va so luong bai viet co Hashtags do
        recyclerViewTags.setAdapter(tagAdapter);

    }
}