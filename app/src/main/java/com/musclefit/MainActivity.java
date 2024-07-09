package com.musclefit;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if the user is logged in
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        if (mAuth.getCurrentUser() != null) {
//            mAuth.signOut();
//        }
        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, open PhoneAuthActivity
            Intent intent = new Intent(this, PhoneAuthActivity.class);
            startActivity(intent);
            finish(); // Close the MainActivity
        } else {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
//            finish(); // Close the MainActivity
        }
    }
}
