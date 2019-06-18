package com.example.hashpotatoesv20.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Main.CreatePostActivity;
import com.example.hashpotatoesv20.Main.MainActivity;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

public class CreateTagActivity extends AppCompatActivity {
    private static final String TAG = "CreateTagActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mTagName, mTagDescription;
    private SwitchCompat mPrivacy;
    private TextView tvPrivacy;
    private UserSettings userSettings;

    private Context mContext = CreateTagActivity.this;

    @Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_createtag, container,false);

        return view;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtag);
        Log.d(TAG, "onCreate: Activity CreateTag started.");
        mTagName = (EditText) findViewById(R.id.tag_name);
        mTagDescription = (EditText) findViewById(R.id.tagDescription);
        tvPrivacy = (TextView) findViewById(R.id.tvPrivacy);
        mPrivacy = (SwitchCompat) findViewById(R.id.privacy);
        mFirebaseMethods = new FirebaseMethods(CreateTagActivity.this);

        setupFirebaseAuth();

        mPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Privacy switch checked");
                if (mPrivacy.isChecked()) {
                    Log.d(TAG, "onClick: Private Tag.");
                    tvPrivacy.setText("Private");
                }
                else {
                    Log.d(TAG, "onClick: Public Tag.");
                    tvPrivacy.setText("Public");
                }
            }
        });

        Button cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing activity, to back to previous screen");
                finish();
            }
        });

        Button create = (Button) findViewById(R.id.btn_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: creating tag.");
                //upload post to firebase
                Toast.makeText(CreateTagActivity.this, "Attempting to create tag", Toast.LENGTH_SHORT).show();

                //check if tag name already exists
                String tag_name = mTagName.getText().toString();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child(getString(R.string.dbname_tags))
                        .orderByChild(getString(R.string.field_tag_name))
                        .equalTo(tag_name);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: ds: " + dataSnapshot.getValue());

                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "onDataChange: Tag name exists.");
                            Toast.makeText(mContext, "Tag name exists. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String tag_desc = mTagDescription.getText().toString();
                            String privacy = tvPrivacy.getText().toString();
                            String tag_name = mTagName.getText().toString();
                            Log.d(TAG, "onDataChange: tag name: " + tag_name + "\ntag desc: " + tag_desc + "\nprivacy: " + privacy);

                            try {
                                mFirebaseMethods.addTagToDatabase(tag_name, tag_desc, privacy);
                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                mContext.startActivity(intent);
                            }
                            catch (NullPointerException e) {
                                Log.e(TAG, "onDataChange: NullPointerException: " + e.getLocalizedMessage() );
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    /*
    ---------------------------------------firebase------------------------------------------------
    */

    /**
     * Setup the firebase auth object
     **/
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                }
                else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database

                //retrieve posts for the user in question

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
