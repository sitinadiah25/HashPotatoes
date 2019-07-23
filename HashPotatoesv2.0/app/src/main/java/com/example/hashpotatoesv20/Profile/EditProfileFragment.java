package com.example.hashpotatoesv20.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Main.CreatePostActivity;
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
import com.google.firebase.database.core.view.Change;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.OnConfirmPasswordListener{

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        //Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            //Check if email is already present
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            Log.d(TAG, "signinSize: " + task.getResult().getSignInMethods().size());
                                            if (task.getResult().getSignInMethods().size() == 1) {
                                                Log.d(TAG, "onComplete: That email is already in use.");
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d(TAG, "onComplete: That email is available.");

                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address is updated.");
                                                                    Toast.makeText(getActivity(), "Email is updated.", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        } catch (NullPointerException e) {
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }
                                    }
                                }
                            });
                        }else{
                            Log.d(TAG, "onComplete: re-authentication failed.");
                        }

                    }
                });
    }

    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    //EditProfile fragment widgets
    private EditText mDisplayName, mUsername, mDescription, mWebsite, mEmail, mYearOfStudy, mMajor;
    private TextView changePhoto;
    private CircleImageView mProfilePhoto;
    private RelativeLayout mParent;

    //variables
    private UserSettings mUserSettings;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container,false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profilePhoto);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mDescription = (EditText) view.findViewById(R.id.description);
        //mWebsite = (EditText) view.findViewById(R.id.website);
        mEmail = (EditText) view.findViewById(R.id.email);
        changePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mYearOfStudy = (EditText) view.findViewById(R.id.year);
        mParent = (RelativeLayout) view.findViewById(R.id.parent_container);
        mMajor = (EditText) view.findViewById(R.id.major);
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

        ImageView checkmark = (ImageView) view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes.");
                saveProfileSettings();
                //getActivity().finish();
                Intent intent = new Intent(mContext, ProfileActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to ChangeProfilePhoto Activity");
                Intent intent = new Intent(mContext, ChangeProfilePhotoActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * also check if the username is unique before submitting to database
     */
    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        //final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final String yearOfStudy = mYearOfStudy.getText().toString();
        final String major = mMajor.getText().toString();

        //case 1: if user made a change to their username
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            Log.d(TAG, "onDataChange: check if username exists");
            checkIfUsernameExists(username);
        }
        //case 2: if user made a change to their emaiil
        //Log.d(TAG, "onDataChange: username exists!");
        if(!mUserSettings.getUser().getEmail().equals(email)){
            //Step 1: Re-authenticate
                 //Confirm the password and email
            //Step 2: Check if the email already is registered
            //'fetchProvidersForEmail
            //Step 3: Change the email
            //Submit the email to the database and authenticate
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);
        }

        /**
         * change non-unique settings
         */
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            //update
            Log.d(TAG, "updateUserAccountSettings: displayname: " + displayName);
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null,null,null);
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            //update
            mFirebaseMethods.updateUserAccountSettings(null,description,null,null,null);

        }
        //if(!mUserSettings.getSettings().getWebsite().equals(website)){
            //update
            //mFirebaseMethods.updateUserAccountSettings(null,null,website,null,null);

        //}
        if(!mUserSettings.getSettings().getYear().equals(yearOfStudy)){
            //update
            mFirebaseMethods.updateUserAccountSettings(null,null,null,yearOfStudy,null);

        }
        if(!mUserSettings.getSettings().getMajor().equals(major)){
            //update
            mFirebaseMethods.updateUserAccountSettings(null,null,null,null,major);

        }
        //getActivity().finish();
    }

    /**
     * check if @param username already exists in the database
     * @param username
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

                if (!dataSnapshot.exists()) {
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        if (singleSnapshot.exists()) {
                            Log.d(TAG, "onDataChange: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                            Toast.makeText(getActivity(), "That username already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        //Log.d(TAG, "setProfileWidgets: settings widgets with data retireved from firebase database");

        mUserSettings = userSettings;
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        //mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mYearOfStudy.setText(settings.getYear());
        mMajor.setText((settings.getMajor()));
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

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
