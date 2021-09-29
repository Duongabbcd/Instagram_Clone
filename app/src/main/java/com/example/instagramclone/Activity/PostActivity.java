package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Adapter.GalleryAdapter;
import com.example.instagramclone.ImageGallery;
import com.example.instagramclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private Uri mImageUri;
    String miUrlOk = "";
    private StorageTask uploadTask;
    StorageReference storageRef;
    Button selector_one ;
    ImageView close, image_added;
    TextView post  , gallery_number ,hey;
    SocialAutoCompleteTextView description;
    ImageView photograph ;
    RecyclerView recyclerView  ,recycle;
    GalleryAdapter galleryAdapter ;
    List<String> images ;

    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    public static final int CAMERA_PERMSSION_CODE = 112  ;
    public static final int STORAGE_PERMISSION_CODE = 113  ;
    public static final int IMAGE_PICK_GALLERY_CODE =1000 ;
    public static final int IMAGE_PICK_CAMERA_CDOE =1001 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        photograph = findViewById(R.id.photograph) ;
        selector_one = findViewById(R.id.selector_one) ;
        recyclerView = findViewById(R.id.recycler_view_gallery) ;
        recycle = findViewById(R.id.recycler_view_chosen) ;
        gallery_number = findViewById(R.id.gallery_number) ;
        hey = findViewById(R.id.hey) ;
        hey.setVisibility(View.GONE);
        storageRef = FirebaseStorage.getInstance().getReference("posts");

        recycle.setHasFixedSize(true);
        recycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent  = new Intent(PostActivity.this, MainActivity.class);
                Toast.makeText(PostActivity.this,"Do you want to have another photo ?",Toast.LENGTH_SHORT).show();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        if(image_added.getVisibility() == View.VISIBLE){
            selector_one.getText().toString().equals("One") ;
        }else if(recycle.getVisibility() == View.VISIBLE){
            selector_one.getText().toString().equals("Multiple") ;
        }

        selector_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selector_one.getText().toString().equals("One")){
                    recycle.setVisibility(View.GONE);
                    image_added.setVisibility(View.VISIBLE);
                }else if(selector_one.getText().toString().equals("Multiple")){
                    recycle.setVisibility(View.VISIBLE);
                    image_added.setVisibility(View.GONE);
                }
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage_10();
            }
        });

        photograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageImportDialog();
            }
        });

      if(ContextCompat.checkSelfPermission(PostActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
          ActivityCompat.requestPermissions(PostActivity.this , new String[]
                  {Manifest.permission.READ_EXTERNAL_STORAGE} ,STORAGE_PERMISSION_CODE);
      }else {
          loadImages();
      }
    }

    private void showImageImportDialog() {
        String[] items = {" Camera" ," Gallery"} ;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select Image") ;
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    checkPermission(Manifest.permission.CAMERA,CAMERA_PERMSSION_CODE);
                    pickCamera();
                }
                if(which == 1){
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE);
                    pickGallery();
                }
            }
        }) ;
        dialog.create().show();  //show dialog
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

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        Date date  =   new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
                        String strDate = format.format(date) ;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postid", postid);
                        hashMap.put("postimage", miUrlOk);
                        hashMap.put("description", description.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("strDate", strDate);
                        hashMap.put("isdisplay",true);
                        reference.child(postid).setValue(hashMap);

                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                        List<String> hashTags = description.getHashtags();
                        if (!hashTags.isEmpty()){
                            for (String tag : hashTags){
                                hashMap.clear();

                                hashMap.put("tag" , tag.toLowerCase());
                                hashMap.put("postid" , postid);

                                mHashTagRef.child(tag.toLowerCase()).child(postid).setValue(hashMap);
                            }
                        }

                        pd.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(PostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        status("online");
        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hashtagAdapter.add(new Hashtag(snapshot.getKey() , (int) snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        description.setHashtagAdapter(hashtagAdapter);
    }


    public void checkPermission(String permission,int requestCode) {
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
                loadImages();
            }
        }
    }

    private void loadImages(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this , 4));
        images = ImageGallery.listOfImages(this) ;
        if(images.size() == 0 ){
            hey.setVisibility(View.VISIBLE);
        }
        galleryAdapter  =new GalleryAdapter(this, images, new GalleryAdapter.PhotoListener() {
            @Override
            public void onPhotoClick(String path) {
                Toast.makeText(PostActivity.this, "" + path, Toast.LENGTH_SHORT).show();
            }
        }) ;

        recyclerView.setAdapter(galleryAdapter);
        gallery_number.setText("Photos ( " + images.size() + " )");
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //ham chay doc lap thuc hien nhiem vu tao ra Uri tu file anh duoc get tu gallery hoac duoc chup
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
                    image_added.setImageURI(mImageUri);
                }
            }
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(getIntent());
            finish();
        }
    }



    public void status(String status){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>() ;
        hashMap.put("status" , status) ;
        ref.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }


}