package com.example.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagramclone.Activity.CommentActivity;
import com.example.instagramclone.Activity.FollowersActivity;
import com.example.instagramclone.Activity.MainActivity;
import com.example.instagramclone.Activity.ProfileActivity;
import com.example.instagramclone.Activity.ShareActivity;
import com.example.instagramclone.Fragment.PostDetailFragment;
import com.example.instagramclone.Model.Comment;
import com.example.instagramclone.Model.Notification;
import com.example.instagramclone.Model.Post;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.Support;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Post> mPosts;
    Support support ;
    FirebaseUser firebaseUser =FirebaseAuth.getInstance().getCurrentUser();
    //FirebaseAuth cap phep truy nhap vao user hien tai
    String fuser =  firebaseUser.getUid() ;
    String originialText  ;
    Translator engVNItranslator ;

    public PostAdapter(Context context, List<Post> posts){
        mContext = context;
        mPosts = posts;
    }

    //1 Post co : postid ,postimage ,description ,publisher
    int i = 0 ;
    @NonNull
    @Override
    public PostAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new PostAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ImageViewHolder holder, final int position) {
        final Post post = mPosts.get(position);
        String publisher = post.getPublisher() ;

        Glide.with(mContext).load(post.getPostimage()) //Lay data cua postImage
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(holder.post_image);

        if (post.getDescription().equals("")){ //neu khong co description thi khong hien phan descripion
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
            holder.description.setEnabled(false);
        }
        String strDate = support.calculateTimeAgo(post.getStrDate()) ;

        holder.post_date.setText(strDate);

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher()); //Ham hien thi thong tin nguoi dang bai viet

        isLiked(post.getPostid(), holder.like); //Ham kuem tra bai viet duoc Like hay chua

        isSaved(post.getPostid(), holder.save); //Ham kiem tra bai viet duoc Save hay chua

        nrLikes(holder.likes, post.getPostid()); //ham hien thi so luot Like cua bai viet

        getComments(post.getPostid(), holder.comments ); //Ham doc so luong comment

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //an lan thu nhat
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())       //Tao ra bang Likes
                            .child(firebaseUser.getUid()).setValue(true); // them du lieu ve luot Like tren Firebase

                    if(!publisher.equals(fuser)){
                        addNotification(post.getPublisher(), post.getPostid()); //hien thong bao ve nguoi nao like bai viet nao
                    }


                } else { //an lan thu hai
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue(); //   xoa du lieu ve luot Like tren Firebase
                }
            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())     //Tao ra bang Saves
                            .child(post.getPostid()).setValue(true); //them du lieu ve luot Save tren Firebase ,bai viet duoc Save tuong ung boi user co uid
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue(); //xoa du lieu ve luot Save
                }
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override // Khi click se chuyen sang ProfileFragment
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);

            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override // Khi click se chuyen sang ProfileFragment
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override // Khi click se chuyen sang ProfileFragment
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
        });
//
        holder.comment.setOnClickListener(new View.OnClickListener() {//Thuc hien Comment
            @Override
            public void onClick(View view) {//chuyen sang CommentActivity va truyen du lieu ve Postid va Publisher sang CommentActivity
                Intent intent = new Intent(mContext.getApplicationContext(), CommentActivity.class); //ham chuyen gai tri den Activity moi
                intent.putExtra("act" ,"act_commet")  ;
                intent.putExtra("postid", post.getPostid()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                intent.putExtra("publisherid", post.getPublisher()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                mContext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() { //Hien thi so luong comment
            @Override
            public void onClick(View view) {//chuyen sang CommentActivity va truyen du lieu ve Postid va Publisher sang CommentActivity
                Intent intent = new Intent(mContext.getApplicationContext(), CommentActivity.class);  //ham chuyen gai tri den Activity moi
                intent.putExtra("act" ,"act_commet")  ;
                intent.putExtra("postid", post.getPostid()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                intent.putExtra("publisherid",post.getPublisher()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                mContext.startActivity(intent);
            }
        });



        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //chuyen sang FollowersActivity
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.main_container,
                        new PostDetailFragment()).commit();
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() { //click vao de hien ra list chuc nang
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.edit:
                                editPost(post.getPostid());//ham edit bai viet
                                return true;
                            case R.id.delete:
                                deletePost(post.getPostid()); //ham xoa bai viet
                                return true ;
                            case R.id.report:
                                Toast.makeText(mContext, "Reported clicked!", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.unfollow:
                                checkUnffolow(post.getPublisher()) ;
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())){ //Neu nguoi dang bai viet khong phai nguoi da dang nhap tren thiet bi
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.unfollow).setVisible(true) ;
                }
                popupMenu.show();
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(post.getPostid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    String commenterid = comment.getPublisher() ;
                    holder.comment_text.setText(comment.getComment());

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(commenterid) ;
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            User user = snap.getValue(User.class) ;
                            holder.commenter_username.setText(user.getUsername());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;

        holder.comment_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext() , CommentActivity.class) ;
                intent.putExtra("postid", post.getPostid()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                intent.putExtra("publisherid", post.getPublisher()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                mContext.startActivity(intent);
            }
        });

        holder.commenter_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Intent intent = new Intent(mContext, ProfileActivity.class);
                mContext.startActivity(intent);
            }
        });
        
        holder.analytic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.total_analytic.setVisibility(View.VISIBLE);
                originialText = post.getDescription() ;
                prepareModel(holder.result_analytic) ;
            }
        });
        holder.exit_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.total_analytic.setVisibility(View.GONE);
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), ShareActivity.class) ;
                intent.putExtra("share_image", post.getPostid()); //cac gia tri duoc chuyen di khi Intent sang Activity moi
                mContext.startActivity(intent);
            }
        });

    }

//    private void sharePost(ImageView img) {
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) img.getDrawable() ;
//        Bitmap bitmap = bitmapDrawable.getBitmap() ;
//        shareImage()
//    }

    private void prepareModel(TextView result){
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build() ;
        engVNItranslator = Translation.getClient(options);

        //
        engVNItranslator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                    translateLanguage(result) ;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }) ;
    }

    private void translateLanguage(TextView result) {
            engVNItranslator.translate(originialText).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s){
                    result.setText(s);
                    System.out.println("Your translating result : "+s);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                        result.setText("Error : " + e.getMessage());
                }
            });
    }

    private void checkUnffolow(String publisherid) {
       DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid);
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               User user = snapshot.getValue(User.class) ;
               if(user.getId().equals(publisherid)) {
                   FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                           .child("following").child(user.getId()).removeValue(); //xoa following acc
                   FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                           .child("followers").child(firebaseUser.getUid()).removeValue(); //xoa follower acc
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }


    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image, like, comment, save, more ,exit_task ,analytic ,share;
        public TextView username, likes, publisher,  comments , post_date ,commenter_username ,comment_text , result_analytic;
        public SocialAutoCompleteTextView description;
        public LinearLayout total_analytic ;


        public ImageViewHolder(View itemView) {
            super(itemView);
            post_date = itemView.findViewById(R.id.post_date);
            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);
            comment_text = itemView.findViewById(R.id.comment_text) ;
            commenter_username = itemView.findViewById(R.id.commenter_username) ;
            share = itemView.findViewById(R.id.share) ;
            
            analytic = itemView.findViewById(R.id.analytic);
            exit_task =itemView.findViewById(R.id.exit_task);
            result_analytic = itemView.findViewById(R.id.result_analytic) ;
            total_analytic = itemView.findViewById(R.id.total_analytics);

        }
    }

    private void addNotification(String userid, String postid){ //Ham tao du lieu vao trong Table Notification
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        //Add vao bang Notifications tren Firebase ,set profileid lam FK ,thuc hien chuc nang add du lieu vao Notification trong database

        if(userid != fuser){
            Date date  =   new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
            String strDate = format.format(date) ;


            HashMap<String, Object> hashMap = new HashMap<>(); //Step 2 : tao bang voi cac thuoc tinh va them gia tri vao tung thuoc tinh tuong ung
            hashMap.put("userid",fuser);
            hashMap.put("text", "liked your post");
            hashMap.put("postid", postid);
            hashMap.put("ispost", true);
            hashMap.put("strDate" , strDate) ;
            hashMap.put("isseen" , false)  ;

            reference.push().setValue(hashMap); //Step 3 : them du lieu vao bang

        }

    }

    private void deletePost(String postId){ //ham xoa bai viet
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Do you want to delete this post ?");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

        alertDialog.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       DatabaseReference reff = FirebaseDatabase.getInstance().getReference("Posts")
                                .child(postId) ; //xoa bai viet tren giao dien user

                        reff.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                HashMap<String ,Object> hashMap = new HashMap<>() ;
                                hashMap.put("isdisplay" ,false) ;
                                snapshot.getRef().updateChildren(hashMap) ;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Intent intent= new Intent(mContext, MainActivity.class) ;
                        mContext.startActivity(intent);
                        Toast.makeText(mContext ,"Let's have another post !",Toast.LENGTH_SHORT).show();


                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());
                        ref1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot snap : snapshot.getChildren()){
                                    Notification notification= snap.getValue(Notification.class);
                                    if(notification.getPostid().equals(postId)){
                                        FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid()).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void nrLikes(final TextView likes, String postId){ //dem so luot like cua bai viet
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() { //cau lenh get Post by Id trong Firebase
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount()+" likes"); // dem so gia tri co torng bang Likes
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getComments(String postId, final TextView comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        reference.addValueEventListener(new ValueEventListener() { //Cau leng get Comment by Id trong Firebase
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comments.setText("View All "+dataSnapshot.getChildrenCount()+" Comments");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userid); //Ham hien thi thong tin ve nguoi dang bai viet


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext.getApplicationContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(final String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override //Lay du lieu bang phuong thuc get Like by postid
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){ //Neu user click nut Like bai viet lan 1  thi :
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Saves").child(firebaseUser.getUid());  //tao table saved danh cho cac bai viet duoc saved tuong ung boi user id hien tai
        reference.addValueEventListener(new ValueEventListener() {
            @Override //Lay du lieu bang phuong thuc get Save by Id of Current User
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()){ //Neu user click nut Save bai viet lan 1 thi :
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                } else{
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void editPost(final String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit Post");

        final EditText editText = new EditText(mContext); //hien thi editText co doan Text trong des voi kich thuoc nhu sau :
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getText(postid, editText); //ham lay description tuong ung voi postid

        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {   //Ham update gia tri trong Firebase

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", editText.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(hashMap);
            }
        });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
    }

    private void getText(String postid, final EditText editText){ //ham doc Desription cua Post tuong ung voi postid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() { //Them su kien de lay du lieu ve
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}