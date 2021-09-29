package com.example.instagramclone.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.instagramclone.Adapter.PostAdapter;
import com.example.instagramclone.Adapter.StoryAdapter;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.Story;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {    // Hiện Story và Post của user và những người user theo dõi
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList; //1 Post co postid ,postimage , description ,publisher

    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList; // 1 Story co : imageurl ,tiemstart,timeend,storyid ,userid

    List<String> followingList;
    List<String> displayPostList ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true); //dao nguoc toan bo Layout
        mLayoutManager.setStackFromEnd(true); //set Recycle View cho post dao nguoc cac item ben trong tao hieu ung doc bai viet tu moi nhat den cu nhat = cach lay the tu duoi len tren
        recyclerView.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);



        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false); //set recycle view cho cac the nam ngang
        recyclerView_story.setLayoutManager(linearLayoutManager);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        followingList = new ArrayList<>();

        displayPostList = new ArrayList<>();


        checkFollowingUsers();


        return view;

    }

    private void checkFollowingUsers(){
            //Ham check vao trong Table Follow kiem tra xem nhanh following tuong ung voi id cua current user
        FirebaseDatabase.getInstance().getReference().child("Follow").child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear(); //set List<String> followingList la rong tai thoi diem load trang HomeFragment
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //vong lap for  lay data tu nhanh follow tren
                    followingList.add(snapshot.getKey()); //ep kieu vao dang sach following voi cac gia tri la Key = gia tri id trong tung nhanh following
                }
                followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid()); //add vao danh sach id cua current user de doc post va xem story cua chinnh minh


              readPosts();
                  readStory();

                System.out.println("Total users can be displayed :" + followingList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void readPosts() {
        //Ham doc bai viet
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            //Lay tat ca bai viet tu table Post
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();           //set List<String> postList la rong tai thoi diem load trang HomeFragment
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { //vong lap for lay data tu table Post
                    Post post = snapshot.getValue(Post.class);   //Lay gia tri tat ca thuoc tinh cua mot Post

                    for (String id : followingList ) { //ep kieu data cho id theo tung gia tri trong list
                            if (post.getPublisher().equals(id)  && post.isIsdisplay() == true){ //Kiem tra thuoc tinh Publisher co cung gia tri voi String id hay khong
                                    postList.add(post);
                            }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readStory(){
        //Ham lay all data cua table Story
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story"); //Lay tata ca story tu table Story
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis(); //lay gia tri thoi gian hien tai
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),""));
                //auto doc duoc story cua chinh minh
                for (String id : followingList) { //ep kieu data cho id theo tung gia tri trong list
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()) { //vong lap for lay data tu table Story tuong ung voi id
                        story = snapshot.getValue(Story.class); // Lay gia tri tat ca thuoc tinh cua 1 Story
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) { //kiem tra xem thoi diem hien tai co nam trong khoang xem duoc Story khong
                            countStory++;
                        }
                    }
                    if (countStory > 0){
                        storyList.add(story); //add Story vao list
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}

