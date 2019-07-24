package com.example.hashpotatoesv20.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.UniversalImageLoader;
import com.example.hashpotatoesv20.dialogs.ConfirmPasswordDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChangePassFragment extends Fragment {

    private static final String TAG = "ChangePassFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile fragment widgets
    private EditText mConfirmNewPass, mNewPass, mCurrentPass;
    private TextView mtvPassword,mtvNewPass, mtvNewCPass;
    private Button mConfirm, mCancel;

    //variables
    private UserSettings mUserSettings;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_changepass, container,false);
        mConfirmNewPass = (EditText) view.findViewById(R.id.confPass);
        mNewPass = (EditText) view.findViewById(R.id.newPass);
        mCurrentPass = (EditText) view.findViewById(R.id.currentpass);
        mtvPassword = (TextView) view.findViewById(R.id.tvPassword);
        mtvNewPass = (TextView) view.findViewById(R.id.tvNewPass);
        mtvNewCPass = (TextView) view.findViewById(R.id.tvNewCPass);
        mCancel = (Button) view.findViewById(R.id.btn_cancel);
        mConfirm = (Button) view.findViewById(R.id.btn_confirm);

        mFirebaseMethods = new FirebaseMethods(getActivity());
        mContext = getActivity();

        setupFirebaseAuth();

        //back arrow for navigating back to "Profile Activity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().onBackPressed();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to change password.");
                changePassword();
                //getActivity().finish();
                Intent intent = new Intent(mContext, ProfileActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: go back to profile");
                Intent intent = new Intent(mContext, ProfileActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * also check if the username is unique before submitting to database
     */
    private void changePassword() {
        final String current_pass = mCurrentPass.getText().toString();
        final String new_pass = mNewPass.getText().toString();
        final String confirm_new_pass = mConfirmNewPass.getText().toString();

        //Check if user current password is correct, if re-authentication is possible
        Log.d(TAG, "changePassword: check current password: " + current_pass);
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), current_pass);

        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            //Check if newpassword and confirm new password is the same
                            if(new_pass.equals(confirm_new_pass)){
                                mAuth.getCurrentUser().updatePassword(new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getActivity(), "Password is updated.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                //not the same, try again
                                Toast.makeText(getActivity(), "Please enter new password again", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.d(TAG, "onComplete: re-authentication failed.");
                            Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    /*
    ---------------------------------------firebase------------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

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
