package com.example.instagramclone.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.instagramclone.Adapter.PostAdapter;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    String postid;

    List<String> idList ;
    Intent intent ;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    DatabaseReference reference ;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        intent = getIntent() ;
        postid = intent.getStringExtra("postid") ;
        recyclerView = findViewById(R.id.recycler_view_detail);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        readPost();

        idList = new ArrayList<>()  ;
    }


    private void readPost() {

        //ham doc chi tiet 1 bai viet
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        //lay gia tri bai viet tuong ung voi postid
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //doc duoc data tu Instance trong Firebase
                postList.clear(); //set list rong tu luc bat dau
                Post post = dataSnapshot.getValue(Post.class); //lay gia tri cac thuoc tinh cua object post
                postList.add(post); //add object vao list boi vi lay duoc duy nhat 1 bai viet tuong duong postid nhu tren nen list nay chi co duy nhat mot object
                postAdapter.notifyDataSetChanged();

                if(post.isIsdisplay() == false){
                    startActivity(new Intent(PostDetailActivity.this , MainActivity.class));
                    Toast.makeText(PostDetailActivity.this ,"This post is deleted !" ,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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


}