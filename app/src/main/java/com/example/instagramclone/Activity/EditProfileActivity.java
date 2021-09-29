package com.example.instagramclone.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.example.instagramclone.Model.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;


public class EditProfileActivity extends AppCompatActivity {

//Activity cho phep update lai thong tin cua Account
    ImageView close, image_profile;
    TextView save, tv_change;
    EditText fullname, username, bio;

    FirebaseUser firebaseUser;
    DatabaseReference reference ;

    private Uri mImageUri; //uniform resource identifier ; chuoi nhan dang tai nguyen thong nhat :dung ten tai nguyen ,dung dia chi hoac dung ca hai
                            // dung Uri de ghi ra File roi su dung file de upload ,Android khong cho phep truc tiep truy cap vao duong dan cua file  .

    private StorageTask uploadTask;
    StorageReference storageRef;

    public static final int CAMERA_PERMSSION_CODE = 112  ;
    public static final int STORAGE_PERMISSION_CODE = 113  ;
    public static final int IMAGE_PICK_GALLERY_CODE =1000 ;
    public static final int IMAGE_PICK_CAMERA_CDOE =1001 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("uploads"); //tao root vao FirebaseStorage mang ten uploads

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        //vao lay gai tri trong talbe Users theo id cua user hien tai
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //ham doc duoc data tu Instance trong Firebase
                User user = dataSnapshot.getValue(User.class); //lay gia tri cua all thuoc tinh trong object User
                fullname.setText(user.getFullname());
                username.setText(user.getUsername());
                bio.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(fullname.getText().toString(),
                        username.getText().toString(),
                        bio.getText().toString());


            }
        });

        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();
            }
        });

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();
            }
        });
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


    private void updateProfile(String fullname, String username, String bio){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        //get data tu talbe Users theo id cua current User
        HashMap<String, Object> map = new HashMap<>();  //map de chua thong tin duoc chinh sua
        map.put("fullname", fullname);  //update gia tri vao tuong ung voi tung thuoc tinh cua Object
        map.put("username", username);
        map.put("bio", bio);

        reference.updateChildren(map); //thuc hien cap nhat thong tin

        Toast.makeText(EditProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(EditProfileActivity.this , ProfileActivity.class));
        finish();
    }

    //Notes : Content Provider - CP - la 1 noi luu tru du lieu ,cho phep cac ung dung co the tao ,trao doi du lieu voi nhau .
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver(); //Content Resolver cung cap phuong thuc CRUD de truy cap vao kho data cua CP ,moi cR bao gom Uri va request
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadImage(){
        //ham upload anh vao avatar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if (mImageUri != null){
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." +getFileExtension(mImageUri)); //thuc hien day vao Storage gia tri thoi diem hien tai va data duoc truyen vao (duoi dang Uri)
            uploadTask = fileReference.putFile(mImageUri); //StorageTask uploadTask thuc hien day file Uri nay vao trong Storage
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl(); //tai xuong 1 Url
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) { //su dung Uri de ghi ra file roi tu file thuc hien upload len he thong
                        Uri downloadUri = task.getResult();
                        String miUrlOk = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        //get user from table Users by id cua current User
                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("imageurl", ""+miUrlOk);
                        reference.updateChildren(map1); //thuc hien update vao trong table Users

                        pd.dismiss();

                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
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
                    uploadImage();
                }
            }
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditProfileActivity.this,MainActivity.class));
            finish();
        }
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

//    @Override
//    protected void onPause() {
//        super.onPause();
//        support.status("offline");
//    }

}