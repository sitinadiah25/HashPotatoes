package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewTagFragment extends Fragment {

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;

    private TextView mPosts, mHashtags, mDisplayName, mYear, mMajor, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private Toolbar toolbar;
    private ImageView profileMenu, heartOutline;
    private BottomNavigationViewEx bottomNavigationView;
    private ListView listView;

    //vars
    private User mUser;

    private Context mContext;
}
