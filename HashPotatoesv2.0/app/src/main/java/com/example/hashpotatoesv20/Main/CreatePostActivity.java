package com.example.hashpotatoesv20.Main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.Permissions;
import com.example.hashpotatoesv20.Utils.SectionsStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mDiscussion, mTag;
    private SwitchCompat mAnonymity;
    private TextView tvAnon;
    private UserSettings userSettings;

    //constants
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = CreatePostActivity.this;

    @Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_createpost, container,false);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createpost);
        Log.d(TAG, "onCreate: CreatePost Activity started.");
        mDiscussion = (EditText) findViewById(R.id.discussion);
        mAnonymity = (SwitchCompat) findViewById(R.id.anonymity);
        mTag = (EditText) findViewById(R.id.searchTag);
        tvAnon = (TextView) findViewById(R.id.tvAnon);
        tvAnon.setText("Public");
        mFirebaseMethods = new FirebaseMethods(CreatePostActivity.this);

        setupFirebaseAuth();

        mAnonymity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Anonymity switch checked");
                if (mAnonymity.isChecked()) {
                    Log.d(TAG, "onClick: Anonymous Post.");
                    tvAnon.setText("Anonymous");
                }
                else {
                    Log.d(TAG, "onClick: Public Post.");
                    tvAnon.setText("Public");
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

        Button share = (Button) findViewById(R.id.btn_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sharing post.");
                //upload post to firebase
                Toast.makeText(CreatePostActivity.this, "Attempting to upload post", Toast.LENGTH_SHORT).show();

                //tokenizing tag string to get the different tags
                String fullTag = mTag.getText().toString();
                String delim = " ";
                StringTokenizer st = new StringTokenizer(fullTag, delim);
                String tag = "";
                while (st.hasMoreElements()) {
                    tag = tag + "#" + st.nextElement() + " ";
                }

                String discussion = mDiscussion.getText().toString();
                String sAnonymity = tvAnon.getText().toString();

                mFirebaseMethods.uploadPost(discussion, tag, sAnonymity);

                Intent intent = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intent);
            }
        });

        /*
        if (checkPermissionsArray(Permissions.PERMISSIONS)) {

        }
        else {
            verifyPermissions(Permissions.PERMISSIONS);
        }*/
    }

    private void setupPost(UserSettings userSettings) {
        UserAccountSettings settings = userSettings.getSettings();

        String username = settings.getUsername();
    }

    private String getUsername(UserSettings userSettings) {
        UserAccountSettings settings = userSettings.getSettings();
        return settings.getUsername();
    }

    /**
     * Verify all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");
        ActivityCompat.requestPermissions(
                CreatePostActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check single permission if it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(CreatePostActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: Permission is not granted for: " + permission);
            return false;
        }
        Log.d(TAG, "checkPermissions: Permission is granted for: " + permission);
        return true;
    }

    private void someMethod() {
        //Properties to Posts (discussion, date, tag, anonymity)

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

                setupPost(mFirebaseMethods.getUserSettings(dataSnapshot));

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
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
