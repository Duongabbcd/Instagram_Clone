package com.example.instagramclone.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.ScanIdCardActivity;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;


public class GetNumberFragment extends Fragment {

         CountryCodePicker countryCodePicker ;
        private CamomileSpinner progressBar ;
        private Button btnGenerateOTP ;
        private EditText edtGetNumber ;
        private String number ;

        RelativeLayout progressLayout ;
        private TextView scanner ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view =inflater.inflate(R.layout.fragment_get_number, container, false);

        countryCodePicker = view.findViewById(R.id.country)  ;
        progressBar = view.findViewById(R.id.progressBar)  ;
        btnGenerateOTP = view.findViewById(R.id.btnGenerateOTP)  ;
        edtGetNumber = view.findViewById(R.id.edtGetNumber)  ;

        progressLayout = view.findViewById(R.id.progress_layout);
        scanner = view.findViewById(R.id.scanner)  ;

      
        initView();
       btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkNumber()){
                    String phoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + number ;
                    sendVerification(phoneNumber);  ;
                }
            }
        });


       scanner.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(getContext() , ScanIdCardActivity.class));
           }
       });
        return  view ;

    }


    private boolean checkNumber() {
        number =edtGetNumber.getText().toString() ;
        if(number.isEmpty()){
           edtGetNumber.setError("Failed");
            return false ;
        }else if(number.length()<10){
            edtGetNumber.setError("Invalid number");
            return false ;
        }else{
           edtGetNumber.setError(null);
            return true ;
        }
    }

    private void initView() {
      countryCodePicker.registerCarrierNumberEditText(edtGetNumber);
        countryCodePicker.getFormattedFullNumber();
      countryCodePicker.isValidFullNumber() ;

    }

    private void sendVerification(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.MILLISECONDS,   // Unit of timeout
                getActivity(),
                verifyPhoneNumber);

    }

        PhoneAuthProvider.OnVerificationStateChangedCallbacks verifyPhoneNumber  = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                else if (e instanceof FirebaseTooManyRequestsException)
                    Toast.makeText(getContext(), "The SMS quota for the project has been exceeded ", Toast.LENGTH_LONG).show();


                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
               progressLayout.setVisibility(View.GONE);
              progressBar.stop();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Toast.makeText(getContext(), "Verification code sent..", Toast.LENGTH_LONG).show();

                Fragment fragment = new VerifyNumberFragment() ;
                Bundle bundle = new Bundle() ;
                bundle.putString(AllConstants.VERIFICATION_CODE ,s);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.login_layout,fragment).commit() ;

              progressLayout.setVisibility(View.VISIBLE);
               progressBar.stop();
            }
        };

}