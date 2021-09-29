package com.example.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
public class RegisterActivity extends AppCompatActivity {
    private EditText username , password , fullname ,email ;
    private Button register ;
    private TextView loginUser ;
    private CheckBox re_hidePass ;

    private ProgressDialog pd ;
    private DatabaseReference reference ;
    private FirebaseAuth Auth  =FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = Auth.getCurrentUser();

    public static final String TAG = "RegisterActivity"  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.re_username);
        fullname=findViewById(R.id.re_name) ;
        email= findViewById(R.id.re_email);
        password = findViewById(R.id.re_password);
        register = findViewById(R.id.re_register) ;
        loginUser=findViewById(R.id.re_loginUser) ;
        re_hidePass = findViewById(R.id.re_hidePass) ;

        reference = FirebaseDatabase.getInstance().getReference();

        re_hidePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        pd = new ProgressDialog(RegisterActivity.this) ;
        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String str_username = username.getText().toString();
                final String str_name = fullname.getText().toString();
                final String str_mail = email.getText().toString();
                final String str_password = password.getText().toString();
                if(str_mail.isEmpty() || !str_mail.contains("@gmail")) {
                    email.setError("Email is not valid!");
                    Toast.makeText(RegisterActivity.this, "All fields are required !", Toast.LENGTH_SHORT).show();
                }else if (str_password.isEmpty() || str_password.length() < 6) {
                   password.setError("Password must be greater than 6 characters!");
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters !", Toast.LENGTH_SHORT).show();
                }else if (str_username.isEmpty()) {
                    username.setError("Username is not valid !");
                    Toast.makeText(RegisterActivity.this, "All fields are required !", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(str_username , str_name ,str_mail,str_password,email);
                }
            }
        });

        LinearLayout linearLayout = findViewById(R.id.register_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();


    }


    private void registerUser(final String username, final String fullname, final String email, String password,final EditText edtEmail ) {

        pd.setMessage("Please Wait!");
        pd.show();

        Auth.createUserWithEmailAndPassword(email , password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            //thuc hien phuong thuc create account by email and password
            public void onSuccess(AuthResult authResult) {

                HashMap<String , Object> map = new HashMap<>();
                //insert value to Object
                map.put("fullname" , fullname);
                map.put("email", email);
                map.put("username" , username.toLowerCase());
                map.put("id" , Auth.getCurrentUser().getUid()); //set PK o day la id cua current user hien tai duoc dang nhap vao bang phuong thuc cu the la email va password
                map.put("bio" , "");
                map.put("status" , "offline")  ;
                map.put("imageurl" , "https://firebasestorage.googleapis.com/v0/b/instagramclone-cd4c3.appspot.com/o/placeholder.png?alt=media&token=40efaa28-d2a0-4312-9e48-68289c27e9a0");
            //reference =FirebaseDatabase.getInstance().getReference()
                //tao su kien sau khi da them data vao bang Users
                reference.child("Users").child(Auth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    //add value vao bang Users set CurrentUsr.getUid lam PK ,thuc hien add Users vao trong table Users cua database
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Update the profile " +
                                    "for better experience", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this , LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);//CLEAR_TASK : Khi start act Register voi Flag nay ,thi du dieu kien nao he thong cung se tao Instance mois va dong thoi destroy all Activity trong Stack
                                                    //CLEAR_TOP : Day old instance len va get data via on new Intent .Destroy all Activity tru cai cuoi cung

                            finish();
                        }else{
                            try{
                                throw task.getException();
                            }catch(FirebaseAuthInvalidCredentialsException e){
                              edtEmail.setError("You can not register by this email!");
                            }catch(FirebaseAuthUserCollisionException e){
                              edtEmail.setError("Please use another email to register!");
                            }catch (Exception e){
                                Log.e(TAG ,e.getMessage() ) ;
                                Toast.makeText(RegisterActivity.this , e.getMessage() , Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }



}