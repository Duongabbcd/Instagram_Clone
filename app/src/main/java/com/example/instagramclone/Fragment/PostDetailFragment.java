package com.example.instagramclone.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.instagramclone.Activity.CommentActivity;
import com.example.instagramclone.Activity.MainActivity;
import com.example.instagramclone.Activity.ProfileActivity;
import com.example.instagramclone.Adapter.PostAdapter;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PostDetailFragment extends Fragment {

    //hien thi chi tiet 1 bai viet -1 Implicit Fragment duoc su dung chuyen giao dien tu PostAdapter moi lan click vao post_image
    String postid , shareid;

    List<String> idList ;


    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE); //lenh hien thi giao dien nay tu cau lenh chuyen giao dien tu mot giao dien khac
        postid = prefs.getString("postid", "none"); //hien thi giao dien post tuong ung voi post id duoc nhan khi thuc hien lenh chuyen giao dien

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        readPost();

        idList = new ArrayList<>()  ;

        return view;
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
                    startActivity(new Intent(getContext() , MainActivity.class));
                    Toast.makeText(getContext(),"This post is deleted !" ,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

