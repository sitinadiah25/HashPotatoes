package com.example.hashpotatoesv20.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int ACTIVITY_NUM = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private TextView mPosts, mHashtags, mDisplayName, mYear, mMajor, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_profile, container,false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mYear = (TextView) view.findViewById(R.id.profile_year);
        mMajor = (TextView) view.findViewById(R.id.profile_major);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profilePhoto);
        mPosts = (TextView) view.findViewById(R.id.tvPost);
        mHashtags = (TextView) view.findViewById(R.id.tvHash);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mWebsite = (TextView) view.findViewById(R.id.profile_website);
        mDescription = (TextView) view.findViewById(R.id.profile_description);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

        setupBottomNavigationView();
        setupToolbar();

        setupFirebaseAuth();

        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to: " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
            }
        });

        Log.d(TAG, "onCreateView: Started.");

        return view;
    }

    private void setProfileWidgets(UserSettings userSettings) {
        //Log.d(TAG, "setProfileWidgets: settings widgets with data retireved from firebase database");

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mHashtags.setText(String.valueOf(settings.getHashtags()));
        mYear.setText(settings.getYear());
        mMajor.setText(String.valueOf(settings.getMajor()));

        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Responsible for setting up the profile Toolbar
     */
    private void setupToolbar() {
        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
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
