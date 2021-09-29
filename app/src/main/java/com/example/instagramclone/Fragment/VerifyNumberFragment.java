package com.example.instagramclone.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.instagramclone.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.ResetPasswordActivity;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class VerifyNumberFragment extends Fragment {

        private PinView otp_text_view ;
        private Button btnVerify ;
        private CamomileSpinner progressBar ;
        private RelativeLayout progressLayout ;


    private String OTP  , pin;
    private FirebaseAuth firebaseAuth ;
    private DatabaseReference databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_verify_number, container, false);

       otp_text_view = view.findViewById(R.id.otp_text_view) ;
       btnVerify = view.findViewById(R.id.btnVerify) ;
       progressBar = view.findViewById(R.id.progressBar) ;
       progressLayout = view.findViewById(R.id.progressLayout) ;


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        Bundle bundle =  getArguments() ;
        if(bundle != null ){
            OTP = bundle.getString(AllConstants.VERIFICATION_CODE);
        }
     btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOTP();
                if (checkOTP()) {
                 progressLayout.setVisibility(View.VISIBLE);
                  progressBar.start();

                    VerifyPine(pin);
                }
            }
        });

       return view ;

    }

    private boolean checkOTP(){
        pin = otp_text_view.getText().toString() ;
        if(pin.isEmpty()){
            otp_text_view.setError("Field is required ! ") ;
            return  false ;
        }else if(pin.length()<6){
            otp_text_view.setError("Invalid code ") ;
            return  false ;
        }else{
            otp_text_view.setError(null);
            return true ;
        }
    }

    private void VerifyPine(String pin){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP, pin);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                  startActivity(new Intent(getContext(),ResetPasswordActivity.class));
                  progressLayout.setVisibility(View.GONE);
                  progressBar.stop();
                }else{
                    Toast.makeText(getContext(),""+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        }) ;
    }
}