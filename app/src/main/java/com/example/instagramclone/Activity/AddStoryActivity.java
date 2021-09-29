package com.example.instagramclone.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    DatabaseReference reference ;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
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
        setContentView(R.layout.activity_add_story);

        storageRef = FirebaseStorage.getInstance().getReference("story");

       showImageImportDialog();

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

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myid);

                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis()+86400000; // 1 day later

                        Date date  =   new Date();
                        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy HH:mm:ss") ;
                        String strDate = format.format(date) ;


                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", miUrlOk);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);
                        hashMap.put("strDate" ,strDate) ;

                        reference.child(storyid).setValue(hashMap);

                        pd.dismiss();

                        finish();

                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(AddStoryActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
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