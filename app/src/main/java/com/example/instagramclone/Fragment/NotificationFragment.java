package com.example.instagramclone.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.example.instagramclone.Adapter.NotificationAdapter;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationFragment extends Fragment {

    //hien thi : text : like your post tu Post Adapter
    //hien thi : text : Started Following you tu UserAdapter and ProfileFragment
    //hien thi : text: commented tu CommentActivity

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    ValueEventListener seenListener ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(notificationAdapter);


        readNotifications();



        return view;
    }


    private void readNotifications(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        //ham doc tat ca cac gia tri co trong Notification tuong ung voi Uid cua Current Usser


             seenListener=  reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) { //doc duoc data tu Instance trong Firebase
                        notificationList.clear(); //tao 1 list rong cho Object Notification
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //vong lap for lay data tu table Notification
                            Notification notification = snapshot.getValue(Notification.class); //lay gia tri tat ca thuoc tinh cua moi Notifi
                            notificationList.add(notification); //add vao trong list notifi
                            if( !notification.isIsseen() ) {
                            Handler handler = new Handler() ;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("isseen", true);
                                    snapshot.getRef().updateChildren(hashMap);

                                }
                            },5000)  ;
                            }else if(notification.getPostid().isEmpty()){
                                snapshot.getRef().removeValue();
                            }


                        }

                        Collections.reverse(notificationList); //reverse toan bo cac the trong List
                        notificationAdapter.notifyDataSetChanged();
//                        seenNotifications();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


    @Override
    public void onPause() {
        super.onPause();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        reference.removeEventListener(seenListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        reference.removeEventListener(seenListener);
    }
}
