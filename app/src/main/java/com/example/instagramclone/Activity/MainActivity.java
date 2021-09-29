package com.example.instagramclone.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Adapter.PostAdapter;
import com.example.instagramclone.Adapter.StoryAdapter;
import com.example.instagramclone.Fragment.HomeFragment;
import com.example.instagramclone.Fragment.NotificationFragment;
import com.example.instagramclone.Fragment.SearchFragment;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.Story;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Support;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    Fragment selectedFragment = null;
    Support support ;
    FirebaseUser  firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference ;
    ValueEventListener seenListener ;
    AppBarLayout bar ;
    private SwitchCompat switcher ;
    ImageView logo ,logo_white , moon ,sun  ,message ;

    ImageView main_home ,main_search,main_post ,main_notif;
    CircleImageView  main_pro ;
    TextView totaler , unreads;

    public static final String MyPREFERENCES = "nightModePrefs" ;
    public static final String KEY_ISNIGHTMODE = "isNightMode" ;
    SharedPreferences sharedPreferences ;

    float x1,x2 ,y1,y2;
    RelativeLayout activity_main ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //de truyen info giua cac Activity hoac Fragment deu can phai tao Intent va tao Bundle ,dung phuong thuc de dong goi du lieu vao Bundle ,va gui thong tin qua lai giua cac Intent
        Bundle intent = getIntent().getExtras();
        //de lay goi thong tin trong Bundle
        if (intent != null){
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            startActivity(new Intent(MainActivity.this ,ProfileActivity.class));
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                    new HomeFragment()).commit();
        }

        sharedPreferences = getSharedPreferences(MyPREFERENCES , Context.MODE_PRIVATE) ;

        bar = findViewById(R.id.bar_main) ;
        bar.setVisibility(View.VISIBLE);
        switcher =findViewById(R.id.switcher) ;
        switcher.setVisibility(View.VISIBLE);
        logo = findViewById(R.id.logo) ;
        logo_white = findViewById(R.id.logo_white) ;
        logo.setVisibility(View.VISIBLE);
        moon = findViewById(R.id.moon)  ;
        sun =findViewById(R.id.sun) ;
        message = findViewById(R.id.message)  ;
        unreads= findViewById(R.id.unreads) ;

        main_home = findViewById(R.id.main_home) ;
        main_search = findViewById(R.id.main_search);
        main_post = findViewById(R.id.main_post) ;
        main_notif = findViewById(R.id.main_notif) ;
        main_pro = findViewById(R.id.main_pro) ;
        totaler = findViewById(R.id.totaler) ;


        activity_main = findViewById(R.id.activity_main);

        userInfo(firebaseUser.getUid());
        checkNightModeActivated();

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveNightModeState(true)  ;
                    recreate();


                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveNightModeState(false)  ;
                    recreate();
                }
            }
        }) ;

        chooseBottom();
        countNotifications();
        countUnreadMsg();

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
//                intent.putExtra("act" , "act_message") ;
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {
                HashMap<String , Object> map = new HashMap<>() ;
                map.put("token" , token) ;
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(firebaseUser.getUid())
                        .updateChildren(map);
            }
        });
    }

    private void saveNightModeState(boolean nightMode) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ISNIGHTMODE ,nightMode) ;
        switcher.isChecked();
        editor.apply();
    }

    public void checkNightModeActivated(){
        if(sharedPreferences.getBoolean(KEY_ISNIGHTMODE , false)){
            switcher.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            logo.setVisibility(View.GONE);
            logo_white.setVisibility(View.VISIBLE);
            moon.setVisibility(View.VISIBLE);

        }else{
            switcher.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            logo.setVisibility(View.VISIBLE);
            logo_white.setVisibility(View.GONE);
            sun.setVisibility(View.VISIBLE);
        }
    }


    private void chooseBottom() {
        main_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                        new HomeFragment()).commit();
                bar.setVisibility(View.VISIBLE);
                isClick(main_home) ;
                clear(main_search,main_post,main_notif);
            }
        });

        main_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                        new SearchFragment()).commit();
                bar.setVisibility(View.GONE);
                isClick(main_search) ;
                clear(main_post,main_home,main_notif);
            }
        });

        main_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedFragment = null;
                Intent intent = new Intent(MainActivity.this,PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                   intent.putExtra("act", "act_post") ;
                startActivity(intent);

                isClick(main_post) ;
                clear(main_search,main_home,main_notif);
            }
        });



        main_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                        new NotificationFragment()).commit();
                bar.setVisibility(View.GONE);
                isClick(main_notif) ;
                clear(main_search,main_home,main_post);
            }
        });


        main_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileid", firebaseUser.getUid());
                editor.apply();

                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
//                intent.putExtra("act" , "act_profile") ;
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

//                    finish();
            }
        });

    }

    private void userInfo(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                System.out.println("Uri Link Here : "+user.getImageurl());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(main_pro);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void countNotifications(){

        reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread1 = 0 ;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if(!notification.isIsseen()){
                        unread1 ++ ;

                    }
                }

                System.out.println("Total unread notifications : " + unread1 );

                String unread = Integer.toString(unread1) ;
                if(unread1 == 0){
                    unreads.setVisibility(View.GONE);
                }else if(unread1 >=10){
                    unreads.setVisibility(View.VISIBLE);
                    unreads.setText("9+");
                }else {
                    unreads.setVisibility(View.VISIBLE);
                    unreads.setText(unread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countUnreadMsg(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread1 = 0 ;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class) ;
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
                        unread1 ++ ;
                    }
                }
                String unread = Integer.toString(unread1) ;
                if(unread1 == 0){
                    unreads.setVisibility(View.GONE);
                }else if(unread1 >=10){
                    unreads.setVisibility(View.VISIBLE);
                    unreads.setText("9+");
                }else {
                    unreads.setVisibility(View.VISIBLE);
                    unreads.setText(unread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void isClick(ImageView imageView){
        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.teal_200));
    }

    private void clear(ImageView imageView1, ImageView imageView2 , ImageView imageView3) {
        imageView1.clearColorFilter();
        imageView2.clearColorFilter();
        imageView3.clearColorFilter();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                x1= event.getX() ;
                y1 =event.getY() ;
                break;
            case MotionEvent.ACTION_UP :
                x2 = event.getX();
                y2 =event.getY() ;
                if(x1 > x2) {
                    Intent i =new Intent(MainActivity.this ,ChatActivity.class) ;
                    startActivity(i);
                    finish();
                }
                break;
        }
        return false ;
    }


    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("status" , status) ;
        reference.updateChildren(hashMap);
    }




    @Override
    protected void onStart() {
        super.onStart();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
       status("offline");
    }

}

