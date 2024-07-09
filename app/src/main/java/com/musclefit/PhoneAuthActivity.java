package com.musclefit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText phoneNumberEditText, verificationCodeEditText;
    private Button sendCodeButton, verifyCodeButton;
    private TextView errorMessageTextView;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;
    private CountryCodePicker countryCodePicker; // Use CountryCodePicker instead of Spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        errorMessageTextView = findViewById(R.id.errorMessageTextView);
        mAuth = FirebaseAuth.getInstance();

        Button skipButton = findViewById(R.id.skipButton);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        countryCodePicker.setDefaultCountryUsingNameCode("CA");
// Set an OnClickListener for the Skip button
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the Skip button click event
                // You can add the redirection logic to your homepage here.
                // For example, you can use an Intent to navigate to your homepage activity.

                // Replace "YourHomepageActivity.class" with your actual homepage activity class.
                Intent intent = new Intent(PhoneAuthActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the callbacks for phone verification
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // Automatically detected verification on some devices.
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // Handle verification failure.
                System.out.println("======================" + e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    displayErrorMessage("Invalid phone number or verification code.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    displayErrorMessage("SMS quota exceeded. Please try again later.");
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS has been sent to the user's phone.
                // Save the verificationId and resending token to use later.
                PhoneAuthActivity.this.verificationId = verificationId;
            }
        };

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected country code
                String selectedCountryCode = countryCodePicker.getSelectedCountryCode();

                // Get the phone number input
                String phoneNumber = phoneNumberEditText.getText().toString().trim();

                // Combine country code and phone number
                String fullPhoneNumber = "+" + selectedCountryCode + phoneNumber;

                // Request verification code
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        fullPhoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        PhoneAuthActivity.this,
                        mCallbacks);

                // Show the verification code input field and verify button
                verificationCodeEditText.setVisibility(View.VISIBLE);
                verifyCodeButton.setVisibility(View.VISIBLE);

                // Clear any previous error messages
                clearErrorMessage();
            }
        });
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the verification code input
                String verificationCode = verificationCodeEditText.getText().toString().trim();

                if (verificationCode.isEmpty()) {
                    displayErrorMessage("Verification code cannot be empty.");
                } else if (verificationId == null || verificationId.isEmpty()) {
                    // Handle the case where verificationId is not set
                    displayErrorMessage("Error verifying code. Please request a new verification code.");
                } else {
                    // Verify the code
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }
        private void displayErrorMessage(String errorMessage) {
        errorMessageTextView.setVisibility(View.VISIBLE);
        errorMessageTextView.setText(errorMessage);
    }

    private void clearErrorMessage() {
        errorMessageTextView.setVisibility(View.GONE);
        errorMessageTextView.setText("");
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();
                            // Additional information to be stored in Firestore
                            String phoneNumber = user.getPhoneNumber();
                            String userId = user.getUid();

                            // Save user information in Firestore
                            saveUserInFirestore(userId, phoneNumber);

                            // Open the HomeActivity or any other activity as needed
                            Intent intent = new Intent(PhoneAuthActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish(); // Close the PhoneAuthActivity
                        } else {
                            // Sign in failed, display an error message
                            displayErrorMessage("Verification failed. Please try again.");
                        }
                    }
                });
    }


    private void saveUserInFirestore(String userId, String phoneNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the user already exists in the 'users' collection based on the phoneNumber
        db.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // User already exists, do not create a new user
                                // You can add additional logic here if needed
                                Log.d("Firestore", "User with phone number already exists");
                            } else {
                                // User does not exist, proceed to save user information
                                Map<String, Object> user = new HashMap<>();
                                user.put("userId", userId);
                                user.put("phoneNumber", phoneNumber);

                                // Add the user to the 'users' collection with the UID as the document ID
                                db.collection("users")
                                        .document(userId)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // User information saved successfully
                                                Log.d("Firestore", "User information saved successfully");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Error saving user information
                                                Log.e("Firestore", "Error saving user information", e);
                                            }
                                        });
                            }
                        } else {
                            // Handle the error
                            Log.e("Firestore", "Error checking for existing user", task.getException());
                        }
                    }
                });
    }




}