package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Fragment.NotificationFragment;
import com.example.instagramclone.Fragment.OldStoryFragment;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Fragment.ProfileFragment;
import com.example.instagramclone.Fragment.SearchFragment;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.ResetPasswordActivity;
import com.example.instagramclone.StartActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {
    //hien thi profile cua 1 account ,Fragment nam tren Bottom va duoc goi den moi khi click vao username ,publisher , image_profile trong 1 post

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference;

    ImageView options, plane;
    ImageView pro_home, pro_search, pro_post, pro_notif, pro_profile;
    TextView username, pro_totaler;
    Button edit_profile;


    String profileid , postid;


    NavigationView nav_view;
    DrawerLayout drawerLayout;
    AppBarLayout profile_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = (ProfileActivity.this).getSharedPreferences("PREFS", MODE_PRIVATE); //lenh hien thi giao dien nay tu cau lenh chuyen giao dien tu mot giao dien khac
        profileid = prefs.getString("profileid", "none"); //hien thi profile tuong ung voi gia tri id duoc nhan trong qua trinh chuyen giao dien
        postid = prefs.getString("postid", "none");


        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("profileid", profileid);
        editor.apply();

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                new ProfileFragment()).commit();
//
//            editor.putString("postid", postid);
//            editor.apply();
//
//            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
//                    new PostDetailFragment()).commit();


        edit_profile = findViewById(R.id.edit_profile);
        username = findViewById(R.id.username);

        options = findViewById(R.id.options);
        plane = findViewById(R.id.plane);
        drawerLayout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        profile_bar = findViewById(R.id.profile_bar);

        pro_home = findViewById(R.id.pro_home);
        pro_search = findViewById(R.id.pro_search);
        pro_post = findViewById(R.id.pro_post);
        pro_notif = findViewById(R.id.pro_notif);
        pro_profile = findViewById(R.id.pro_profile);
        pro_totaler = findViewById(R.id.pro_totaler);


        userInfo();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // set item as selected to persist highlight
                        item.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        switch (item.getItemId()) {
                            case R.id.edit_profile_account:
                                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                                finish();
                                break;
                            case R.id.old_story:
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                                        new OldStoryFragment()).commit();
                                break;
                            case R.id.reset_password:
                                startActivity(new Intent(ProfileActivity.this, ResetPasswordActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                break;
                            case R.id.qr_code:
                                Toast.makeText(ProfileActivity.this, "QR  !", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.log_out:
                                FirebaseAuth.getInstance().signOut(); //thuc hien chuc nang logout khoi acc hien tai tren device
                                Intent intent = new Intent(ProfileActivity.this, StartActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                        }
                        return true;
                    }
                });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()) ;
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user  =snapshot.getValue(User.class) ;
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(pro_profile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        plane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        countNotifications();
        chooseBottom();
    }

    private void chooseBottom() {
        pro_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                isClick(pro_home);
                clear(pro_notif, pro_search, pro_post);
            }
        });

        pro_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                        new SearchFragment()).commit();
                profile_bar.setVisibility(View.GONE);

                isClick(pro_search);
                clear(pro_home, pro_notif, pro_post);
            }
        });

        pro_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PostActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                isClick(pro_post);
                clear(pro_home, pro_search, pro_notif);
            }
        });

        pro_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,
                        new NotificationFragment()).commit();
                profile_bar.setVisibility(View.GONE);

                isClick(pro_notif);

            }
        });

        pro_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }

        });

    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (ProfileActivity.this == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(pro_profile);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void countNotifications() {

        reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread1 = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (!notification.isIsseen()) {
                        unread1++;

                    }
                }

                String unread = Integer.toString(unread1) ;
                if(unread1 == 0){
                    pro_totaler.setVisibility(View.GONE);
                }else if(unread1 >=10){
                    pro_totaler.setVisibility(View.VISIBLE);
                    pro_totaler.setText("9+");
                }else {
                    pro_totaler.setVisibility(View.VISIBLE);
                    pro_totaler.setText(unread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isClick(ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.teal_200));

    }

    private void clear(ImageView imageView1, ImageView imageView2, ImageView imageView3) {
        imageView1.clearColorFilter();
        imageView2.clearColorFilter();
        imageView3.clearColorFilter();

    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
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