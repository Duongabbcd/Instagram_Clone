package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.instagramclone.Adapter.MessageAdapter;
import com.example.instagramclone.Model.Chat;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    CircleImageView message_avatar ;
    TextView username ,status ;

    FirebaseUser firebaseUser   = FirebaseAuth.getInstance().getCurrentUser();

    DatabaseReference reference ;
    MessageAdapter messageAdapter   ;
    List<Chat> mChat ;
    boolean notify = false ;
    RecyclerView recyclerView ;
    ValueEventListener seenListener ;

    Intent intent;
    ImageButton btn_send ;
    EditText text_send ;
    String userid ,token  ,name ,image;
    ImageView wester  ,photographer , attachement;

    private Uri mImageUri;
    String miUrlOk = "";
    private StorageTask uploadTask;
    StorageReference storageRef;


    public static final int CAMERA_PERMSSION_CODE = 112  ;
    public static final int STORAGE_PERMISSION_CODE = 113  ;
    public static final int IMAGE_PICK_GALLERY_CODE =1000 ;
    public static final int IMAGE_PICK_CAMERA_CDOE =1001 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar_message) ;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        message_avatar = findViewById(R.id.message_avatar) ;
        username = findViewById(R.id.username);
        btn_send  =  findViewById(R.id.btn_send) ;
        text_send = findViewById(R.id.text_send) ;
        wester = findViewById(R.id.wester) ;
        status = findViewById(R.id.status) ;
        photographer= findViewById(R.id.photographer);
        attachement=findViewById(R.id.attachment) ;

        recyclerView = findViewById(R.id.recycler_view_chat )  ;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext()) ;
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userid") ;
        token= intent.getStringExtra("token") ;
        name = intent.getStringExtra("name");


        System.out.println("User ID is : " + userid);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)  ;


        storageRef = FirebaseStorage.getInstance().getReference("message");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class) ;
                username.setText(user.getUsername());

                Glide.with(getApplicationContext()).load(user.getImageurl()).into(message_avatar) ;
                status.setText(user.getStatus());

                readMessage(firebaseUser.getUid() ,userid , user.getImageurl()) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })  ;

        wester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MessageActivity.this,ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            Date date  = new Date() ;
            SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
            String strDate = format.format(date) ;

            @Override
            public void onClick(View v) {
                notify = true ;

                String msg = text_send.getText().toString() ;
                if(!msg.equals("")){
                    sendMessage(firebaseUser.getUid(),userid , msg,strDate) ;
                    sendNotification(name , msg , token);

                }else{
                    Toast.makeText(MessageActivity.this, "You can't send empty message ! ", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");

            }
        });

        message_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileid", userid);
                editor.apply();

                Intent intent = new Intent(MessageActivity.this,ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileid", userid);
                editor.apply();

                Intent intent = new Intent(MessageActivity.this,ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        final Handler handler = new Handler() ;
        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               status("typing...");
               handler.removeCallbacksAndMessages(null);
               handler.postDelayed(userStoppedTyping ,1000) ;
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    status("online");
                }
            };
        });

        photographer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA,CAMERA_PERMSSION_CODE);
                pickCamera();
            }
        });
        attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE);
                pickGallery();
            }
        });

        seenMessage(userid);

    }



    private void sendNotification(String name, String msg, String token) {
        try{
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data= new JSONObject() ;
            data.put("title", name) ;
            data.put("body"  , msg) ;

            JSONObject notifactionData = new JSONObject() ;
            notifactionData.put("notification" , data) ;
            notifactionData.put("to" , token) ;

            JsonObjectRequest request = new JsonObjectRequest(url, notifactionData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(MessageActivity.this, "Success ", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MessageActivity.this, error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    System.out.println("Error : "  +  error.getLocalizedMessage());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String ,String> map = new HashMap<>() ;
                    String key = "Key=AAAAr4wgW8A:APA91bFEcYH5efzCUuDL-ajvcgblDGy4Df6FtRxutFJw0MEpYcOyRZE_rBO8mGPupLn7vvwawdEx8XYa9jBzF64wSTuCZ4VAdtGEXaRyt7_3K5YJbscxLRRhcSEzcUDSg7Gl7dcb5-i4";
                   map.put("Authorization" , key) ;
                    map.put("Content-Type", "application/json") ;
                    return map;
                }
            };
            queue.add(request) ;
        }catch (Exception ex){

        }
    }


    //Notes : Content Provider - CP - la 1 noi luu tru du lieu ,cho phep cac ung dung co the tao ,trao doi du lieu voi nhau .
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver(); //Content Resolver cung cap phuong thuc CRUD de truy cap vao kho data cua CP ,moi cR bao gom Uri va request
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage_10(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri != null){
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        miUrlOk = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

                        Date date  =   new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
                        String strDate = format.format(date) ;
                        String messageid =  reference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("feeling" , -1)  ;
                        hashMap.put("sender" ,myid) ;
                        hashMap.put("receiver" ,userid) ;
                        hashMap.put("message" ,"have sent a photo") ;
                        hashMap.put("strDate" ,strDate) ;
                        hashMap.put("isseen" ,false) ;
                        hashMap.put("photoUrl" ,miUrlOk);
                        hashMap.put("link","");
                        hashMap.put("messageid",messageid);

                        reference.child(messageid).setValue(hashMap) ;

                        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatlist")
                                .child(firebaseUser.getUid())
                                .child(userid) ;
                        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!snapshot.exists()){
                                    chatRef.child("id").setValue(userid) ;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final DatabaseReference chatRefReceiver =  FirebaseDatabase.getInstance().getReference("chatlist")
                                .child(userid)
                                .child(firebaseUser.getUid()) ;
                        chatRefReceiver.child("id").setValue(firebaseUser.getUid()) ;

                        pd.dismiss();
   
                    } else {
                        Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(MessageActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //got image from camera now crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this); //enable iamge guidlines
            }
            if(requestCode == IMAGE_PICK_CAMERA_CDOE){
                //got iamge from camera now crop it
                CropImage.activity(mImageUri).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this); //enable iamge guidlines
            }

            //get croped image
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data) ;
                if(resultCode ==RESULT_OK) {
                    mImageUri  = result.getUri(); // get iamge uri
                    uploadImage_10();
                }
            }
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
        }
    }

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},requestCode);
        } else {
            Toast.makeText(this, "Wait a second my friend ! ", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMSSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickCamera();
            }else{
                Toast.makeText(this,"Failed !" ,Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickGallery();
            }
        }
    }

    private void pickGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK) ;
        //set intent type to iamge
        intent.setType("image/*") ;
        startActivityForResult(intent ,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //itent to take iamge form camera ,it will also be save to storage to get high quality iamge
        ContentValues values = new ContentValues() ;
        values.put(MediaStore.Images.Media.TITLE , "New Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION ,"Image to text") ;
        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,values) ;

        Intent cameraIntent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE) ;
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT ,mImageUri) ;
        startActivityForResult(cameraIntent ,IMAGE_PICK_CAMERA_CDOE);

    }


    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String , Object> hashMap = new HashMap<>() ;
                        hashMap.put("isseen" , true) ;
                        dataSnapshot.getRef().updateChildren(hashMap) ;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })  ;
    }

    private void sendMessage(String sender, String receiver, String msg, String date) {
        DatabaseReference reference  =  FirebaseDatabase.getInstance().getReference() ;
        String messageid =  reference.push().getKey();
        HashMap<String ,Object> hashMap = new HashMap<>() ;
        hashMap.put("feeling" , -1)  ;
        hashMap.put("sender" ,sender) ;
        hashMap.put("receiver" ,receiver) ;
        hashMap.put("message" ,msg) ;
        hashMap.put("strDate" ,date) ;
        hashMap.put("isseen" ,false) ;
        hashMap.put("photoUrl" ,"default");
        hashMap.put("link","");
        hashMap.put("messageid" , messageid) ;
        reference.child("Chats").child(messageid).setValue(hashMap) ;

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatlist")
                .child(firebaseUser.getUid())
                .child(userid) ;
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid) ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRefReceiver =  FirebaseDatabase.getInstance().getReference("chatlist")
                .child(userid)
                .child(firebaseUser.getUid()) ;
        chatRefReceiver.child("id").setValue(firebaseUser.getUid()) ;

        final String message = msg ;

    }

    private void readMessage(String myid, String userid, String imageurl) {
            mChat = new ArrayList<>()  ;

            reference =FirebaseDatabase.getInstance().getReference("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mChat.clear() ;
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Chat chat =dataSnapshot.getValue(Chat.class);
                        if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                        chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                            mChat.add(chat) ;
                        }


                        messageAdapter = new MessageAdapter(MessageActivity.this, mChat ,imageurl) ;
                        recyclerView.setAdapter(messageAdapter);
                      if(mChat.size() >1){
                          recyclerView.smoothScrollToPosition(mChat.size() -1 );
                      }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }) ;
    }

    public void status(String status){
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("status" , status) ;
        ref1.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
            status("offline");
    }

    @Override
    protected void onStop() {
        super.onStop();
        reference.removeEventListener(seenListener);
    }
}