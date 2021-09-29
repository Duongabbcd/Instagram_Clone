package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Activity.StoryActivity;
import com.example.instagramclone.Activity.StoryDetailActivity;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Model.Story;
import com.example.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class OldStoriesAdapter extends RecyclerView.Adapter<OldStoriesAdapter.ViewHolder> {
    private Context mContext ;
    private List<Story> stories ;

    public OldStoriesAdapter(Context mContext, List<Story> stories) {
        this.mContext = mContext;
        this.stories = stories;
    }

    String fuser = FirebaseAuth.getInstance().getCurrentUser().getUid() ;

    @NonNull
    @Override
    public OldStoriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.item_old_stories,parent ,false) ;
      return new OldStoriesAdapter.ViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull OldStoriesAdapter.ViewHolder holder, int i) {
            final Story story = stories.get(i) ;
        Glide.with(mContext).load(story.getImageurl()).into(holder.content) ;

        holder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create() ;
                alertDialog.setTitle("Are you sure you want to delete this story");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(story.getStoryid());
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(mContext , "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return false ;
            }
        });
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, StoryDetailActivity.class);
                intent.putExtra("oldstoryid", story.getStoryid());
               mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView content ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.story_content) ;
        }
    }
}
