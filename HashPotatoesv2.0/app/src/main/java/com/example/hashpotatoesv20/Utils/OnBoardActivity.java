package com.example.hashpotatoesv20.Utils;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.hashpotatoesv20.Login.LoginActivity;
import com.example.hashpotatoesv20.R;
import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class OnBoardActivity extends TutorialActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new Step.Builder().setTitle("Welcome to HashPotatoes")
                .setContent("HashPotatoes is a platform where students can carry out discussions without the fear of judgement.")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.welcome1) // int top drawable
                .setSummary("")
                .build());

        addFragment(new Step.Builder().setTitle("Discuss")
                .setContent("Create posts where you can discuss school-related material anonymously.")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.welcome2) // int top drawable
                .setSummary("")
                .build());

        addFragment(new Step.Builder().setTitle("Tag")
                .setContent("Create custom tags for posts. \nTags act like groups - private tags and its posts are only visible to its followers while public tags are visible to all. \nAll posts should be tagged so that other users are able to view them.")
                .setBackgroundColor(Color.parseColor("#ABD1ED")) // int background color
                .setDrawable(R.drawable.welcome3) // int top drawable
                .setSummary("")
                .build());

    }

    @Override
    public void finishTutorial() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void currentFragmentPosition(int position) {

    }
}
