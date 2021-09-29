package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Adapter.CommentAdapter;
import com.example.instagramclone.Model.Comment ;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisherid;
    DatabaseReference reference ;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    CircleImageView author_avatar ;
    TextView author_username ,author_des ,author_date ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

//        status("online");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments"); //set Title tren toolbar la Comments
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //set nut quay tro lai trang truoc
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent(); //ham nhan gia tri tu lenh Intent cua giao dien cu
        postid = intent.getStringExtra("postid"); //nhan gia tri post id khi duoc Intent tu giao dien cu
        publisherid = intent.getStringExtra("publisherid"); //nhan gia tri  publisherid khi duoc Intent tu giao dien cu

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid);
        recyclerView.setAdapter(commentAdapter);

        post = findViewById(R.id.post);
        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);



        author_avatar  = findViewById(R.id.author_avatar) ;
        author_date = findViewById(R.id.author_date) ;
        author_des = findViewById(R.id.author_des );
        author_username = findViewById(R.id.author_username)  ;

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(publisherid) ;
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User us = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(us.getImageurl()).into(author_avatar) ;
                author_username.setText(us.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post po = snapshot.getValue(Post.class) ;
                author_des.setText(po.getDescription());
                author_date.setText(calculateTimeAgo(po.getStrDate()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })  ;



        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addcomment.getText().toString().equals("")){ //Neu click vao Edittext ma khong comment gi thi :
                    Toast.makeText(CommentActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    addComment(); //thuc hien lenh add Comment vao trong database
                }
            }
        });

        getImage();
        readComments();

    }

    private void addComment(){
        //Ham tao du lieu vao trong table Comments

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        //Add vao bang Comments tren database ,set postid lam FK, thuc hien chuc nang add du lieu vao Comments trong database
        String commentid = reference.push().getKey();

        Date date  =   new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
        String strDate = format.format(date) ;
        //set PK cho moi object Comment la 1 commentid  ,khi push ,Google se tu sinh ra 1 unique code
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString()); //insert value vao trong table Comments
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentid", commentid);
        hashMap.put("strDate" , strDate)  ;

        reference.child(commentid).setValue(hashMap);
        String fuser = firebaseUser.getUid() ;
        if(!publisherid.equals(fuser)){
            addNotification();
        }
        addcomment.setText("");

    }

    private void addNotification(){//ham them Noti vao trong table Notifications
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);
        //Add vao bang Notifications tren Firebase ,set profileid lam FK ,thuc hien chuc nang add du lieu vao Notification trong database
        if(publisherid != firebaseUser.getUid()){
            Date date  =   new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
            String strDate = format.format(date) ;


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userid", firebaseUser.getUid());
            hashMap.put("text", "commented: "+addcomment.getText().toString()); //insert value vao trong table Noti
            hashMap.put("postid", postid);
            hashMap.put("ispost", true);
            hashMap.put("strDate" , strDate) ;
            hashMap.put("isseen" , false)  ;

            reference.push().setValue(hashMap); //Step 3 : them du lieu vao bang
        }


    }

    private void getImage(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        //get thong tin cua User hien tai tu bang Users
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //doc duoc data tu Instance trong Firebase
                User user = dataSnapshot.getValue(User.class); //lay gia tri all thuoc tinh cua 1 object User
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile); //ham doc anh bang thu vien Glide
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        //get all Comments by post id
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {//doc duoc data tu Instance trong Firebase
                commentList.clear(); //set list comment clear
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ //vong lap for lay al gia tri trong table
                    Comment comment = snapshot.getValue(Comment.class); //lay gia tri all thuoc tinh cua 1 object Comment
                    commentList.add(comment); //add vao list
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String calculateTimeAgo(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
        String suffix = "ago";
        try
        {
            Date past = sdf.parse(strDate);
            Date now = new Date();

            System.out.println(now);
            System.out.println(past);
            long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if(seconds < 20){
                return "just now"  ;
            }
            else if (seconds < 60 && seconds >=20) {
                return seconds + " Seconds " + suffix;
            } else if (minutes < 60) {
                return minutes + " Minutes "+suffix;
            } else if (hours < 24) {
                return hours + " Hours "+suffix;
            } else if (days>= 7) {
                if (days > 360) {
                    return (days / 360) + " Years " + suffix;
                } else if (days > 30) {
                    return (days / 30) + " Months " + suffix;
                } else {
                    return (days / 7) + " Week " + suffix;
                }
            } else if (days < 7) {
                return days+" Days "+suffix;
            }
        }
        catch (Exception j){
            j.printStackTrace();
        }
        return null ;
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

}