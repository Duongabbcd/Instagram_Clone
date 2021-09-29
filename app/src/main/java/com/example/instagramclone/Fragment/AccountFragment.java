package com.example.instagramclone.Fragment;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.instagramclone.Adapter.ChatAdapter;
import com.example.instagramclone.Adapter.SearchMsgAdapter;
import com.example.instagramclone.Model.Chat;
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
import java.util.List;


public class AccountFragment extends Fragment {
    EditText acc_search ;
    private List<Chat> mChats ;
    private SearchMsgAdapter msgAdapter ;
    private List<User> mUsers ;
    private ChatAdapter chatAdapter ;

    private RecyclerView recyclerView ,recyclerMsg ;
    ImageView finisher ;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    String fuser = firebaseUser.getUid() ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        recyclerView = view.findViewById(R.id.acc_recycle_view) ;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerMsg = view.findViewById(R.id.msg_recycle_view) ;
        recyclerMsg.setHasFixedSize(true);
        recyclerMsg.setLayoutManager(new LinearLayoutManager(getContext()));
        acc_search = view.findViewById(R.id.acc_search);

        finisher = view.findViewById(R.id.acc_finish) ;
        mUsers = new ArrayList<>();
        chatAdapter= new ChatAdapter(getContext(), mUsers,false) ;
        readUsers();

        acc_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString()) ;
                System.out.println("Input  : " + s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchMessage(s.toString());

            }
        });

        finisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             getFragmentManager().beginTransaction().replace(R.id.frame_message,
                        new AccountFragment()).commit();
            }
        });

        return view ;
    }

    private void readUsers() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(acc_search.getText().toString().equals("")){
                    mUsers.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null ;
                        assert firebaseUser != null ;
                        if(!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(chatAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUser(String s) {
        Query query =  FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s).endAt(s+"\uf8ff") ;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class) ;

                    if(!user.getId().equals(fuser)){
                        mUsers.add(user)  ;
                    }
                }

                chatAdapter= new ChatAdapter(getContext() , mUsers ,false)  ;
                recyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
    }

    private void searchMessage(String s){
        mChats = new ArrayList<>();
        Query querry = FirebaseDatabase.getInstance().getReference("Chats").orderByChild("message").startAt(s).endAt(s+"\uf8ff") ;

        querry.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChats.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) || chat.getSender().equals(firebaseUser.getUid())){
                        mChats.add(chat) ;
                    }
                }
                msgAdapter = new SearchMsgAdapter(getContext() , mChats) ;
                recyclerMsg.setAdapter(msgAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
    }
}