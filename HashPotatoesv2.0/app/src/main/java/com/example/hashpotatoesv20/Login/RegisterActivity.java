package com.example.hashpotatoesv20.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.User;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, username, password, confPassword;
    private EditText mEmail, mPassword, mUsername, mConfirmPassword;
    private TextView loadingPleaseWait, mError, mBack;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private LinearLayout mParent;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);
        Log.d(TAG, "onCreate: started.");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                hideSoftKeyboard();
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                username = mUsername.getText().toString();
                confPassword = mConfirmPassword.getText().toString();

                //check if email is valid
                if (!isEmailValid(email)) {
                    mError.setText("Email is invalid.");
                    mError.setVisibility(View.VISIBLE);
                }
                else {
                    mError.setText("");
                    mError.setVisibility(View.GONE);
                    //check if username exists
                    checkIfUsernameExists(username);
                    if (!mError.getText().toString().equals("Username already exists.")) {
                        //check if passwords are the same
                        if (password.equals(confPassword)) {
                            if(checkInputs(email,password,username)){
                                mProgressBar.setVisibility((View.VISIBLE));
                                loadingPleaseWait.setVisibility(View.VISIBLE);

                                firebaseMethods.registerNewEmail(email, password, username);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                mProgressBar.setVisibility((View.GONE));
                                loadingPleaseWait.setVisibility(View.GONE);
                            }
                        }
                        else {
                            mError.setText("Passwords are not the same.");
                            mError.setVisibility(View.VISIBLE);
                            //Toast.makeText(mContext, "Passwords are not the same.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to login screen");
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    //check if email is valid
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean checkInputs(String email, String password, String username){
        Log.d(TAG,"checkInputsL checking inputs for null values");
        if(email.equals("") || username.equals("") || password.equals("")) {
            mError.setText("All fields must be filled out");
            mError.setVisibility(View.VISIBLE);
            //Toast.makeText(mContext, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    /**
     * initializing activity widgets
     */
    private void initWidgets() {
        Log.d(TAG, "initWidgets: Initializing widgets");
        mEmail = (EditText) findViewById(R.id.inputEmail);
        mPassword = (EditText) findViewById(R.id.inputPassword);
        mConfirmPassword = (EditText) findViewById(R.id.inputConfirmPassword);
        mUsername = (EditText) findViewById(R.id.inputName);
        btnRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        loadingPleaseWait = (TextView) findViewById(R.id.loadingPleaseWait);
        mError = (TextView) findViewById(R.id.error_msg);
        mBack = (TextView) findViewById(R.id.back_btn);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
        mParent = (LinearLayout) findViewById(R.id.layout_parent);
        setupUI(mParent);
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking if string is null");
        if (string.equals("")) {
            return true;
        }
        return false;
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    /*
    ---------------------------------------firebase------------------------------------------------
     */

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + "already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = false;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        mError.setText("");
                        mError.setVisibility(View.GONE);
                        exists = true;
                    }
                }
                if (exists) {
                    mError.setText("Username already exists.");
                    mError.setVisibility(View.VISIBLE);
                }
//                else {
//                    mError.setText("Username already exists.");
//                    mError.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //hide keyboard if user touches view
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Log.d(TAG, "onAuthStateChanged: user: " + user);

                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        //success method
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        //error method
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();
                }
                else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
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
