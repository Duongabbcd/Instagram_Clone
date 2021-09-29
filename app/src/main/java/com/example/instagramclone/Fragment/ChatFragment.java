package com.example.instagramclone.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.instagramclone.Adapter.ChatAdapter;
import com.example.instagramclone.Adapter.SearchMsgAdapter;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.Chatlist;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView  ;
    private List<User> mUsers ;
    private ChatAdapter chatAdapter ;

    private DatabaseReference reference ;

    FirebaseUser firebaseUser  = FirebaseAuth.getInstance().getCurrentUser();
    String fuser = firebaseUser.getUid() ;

    private List<Chatlist>  userList ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_chat, container, false);

       recyclerView =view.findViewById(R.id.chat_recycle_view) ;
       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

       userList = new ArrayList<>();

       chatlist();

       return  view ;
    }

    private void chatlist() {
        reference = FirebaseDatabase.getInstance().getReference("chatlist").child(fuser) ;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chatlist chatlist = dataSnapshot.getValue(Chatlist.class);
                    userList.add(chatlist) ;
                }

                readChats() ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
    }


    private void readChats() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users") ;

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    for(Chatlist chatlist : userList){
                        if(user.getId().equals(chatlist.getId())){
                            mUsers.add(user) ;
                        }
                    }
                }
                chatAdapter = new ChatAdapter(getContext(),mUsers,true) ;
                Collections.reverse(mUsers);
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}