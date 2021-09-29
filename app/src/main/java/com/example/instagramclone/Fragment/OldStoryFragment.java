package com.example.instagramclone.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instagramclone.Adapter.OldStoriesAdapter;
import com.example.instagramclone.Model.Story;
import com.example.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OldStoryFragment extends Fragment {
    RecyclerView recycler_view_stories ;
    private OldStoriesAdapter oldStories ;
    private List<Story> storyList ;

    FirebaseUser firebaseUser ;
    private List<String> myStories ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view=  inflater.inflate(R.layout.fragment_old_story, container, false);

        recycler_view_stories = view.findViewById(R.id.recycler_view_stories)  ;
        recycler_view_stories.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =  new GridLayoutManager(getContext() , 3) ;
        recycler_view_stories.setLayoutManager(linearLayoutManager);

        storyList = new ArrayList<>() ;
        oldStories = new OldStoriesAdapter(getContext() ,storyList) ;
        recycler_view_stories.setAdapter(oldStories);

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser() ;
        myOldStories();

        return  view ;
    }

    private void myOldStories() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Story story = dataSnapshot.getValue(Story.class);
                            storyList.add(story) ;
                }
                Collections.reverse(storyList);
                oldStories.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}