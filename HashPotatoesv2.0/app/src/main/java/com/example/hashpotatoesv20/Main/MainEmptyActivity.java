package com.example.hashpotatoesv20.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.hashpotatoesv20.Login.LoginActivity;
import com.example.hashpotatoesv20.Utils.OnBoardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainEmptyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Intent activityIntent;

        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");
            // first time task (open onboardactivity)
            activityIntent = new Intent(this, OnBoardActivity.class);// record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
        }        else {
            // go straight to main if a token is stored
            if (user != null) {
                activityIntent = new Intent(this, MainActivity.class);
            } else {
                activityIntent = new Intent(this, LoginActivity.class);
            }
        }

        startActivity(activityIntent);
        finish();
    }
}