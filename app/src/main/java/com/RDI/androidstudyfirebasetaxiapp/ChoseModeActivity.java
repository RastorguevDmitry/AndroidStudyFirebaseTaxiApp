package com.RDI.androidstudyfirebasetaxiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.RDI.androidstudyfirebasetaxiapp.R;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

public class ChoseModeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_mode);

    }

    public void goToPassengerSignIn(View view) {
        startActivity(new Intent(ChoseModeActivity.this, PassengerSignInActivity.class));
    }

    public void goToDriverSignIn(View view) {
        startActivity(new Intent(ChoseModeActivity.this, DriverSignInActivity.class));
    }
}
