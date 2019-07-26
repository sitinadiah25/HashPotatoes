package com.example.hashpotatoesv20.Main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hashpotatoesv20.Login.LoginActivity;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Profile.AccountSettingsActivity;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.MainfeedListAdapter;
import com.example.hashpotatoesv20.Utils.SectionsPagerAdapter;
import com.example.hashpotatoesv20.Utils.UniversalImageLoader;
import com.example.hashpotatoesv20.Utils.ViewPostFragment;
import com.example.hashpotatoesv20.Utils.ViewTagFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements
        MainfeedListAdapter.OnLoadMoreItemsListener,
        MainFragment.onListPostSelectedListener,
        MainfeedListAdapter.OnTagSelectedListener {

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        MainFragment fragment = (MainFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher: " + R.id.container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            Log.d(TAG, "onLoadMoreItems: fragment not empty.");
            fragment.displayMorePosts();
        }
    }

    @Override
    public void onPostSelected(Post post, int activity_number) {
        Log.d(TAG, "onPostSelected: selected a post from listview " + post.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.relLayoutParent, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    public void OnTagSelected(final String Tag) {
        Log.d(TAG, "OnTagSelected: setting up view tag fragment.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_tags));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if(singleSnapshot.child(mContext.getString(R.string.field_tag_name)).getValue().toString().equals(Tag.substring(1))) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        com.example.hashpotatoesv20.Models.Tag tag = new Tag();
                        tag.setPrivacy(objectMap.get(mContext.getString(R.string.field_privacy)).toString());
                        tag.setTag_name(objectMap.get(mContext.getString(R.string.field_tag_name)).toString());
                        tag.setTag_id(objectMap.get(mContext.getString(R.string.field_tag_id)).toString());
                        tag.setTag_description(objectMap.get(mContext.getString(R.string.field_tag_description)).toString());
                        tag.setOwner_id(objectMap.get(mContext.getString(R.string.field_owner_id)).toString());

                        Log.d(TAG, "onDataChange: get tag: " + tag.toString());
                        ViewTagFragment fragment = new ViewTagFragment();
                        Bundle args = new Bundle();
                        args.putParcelable(getString(R.string.field_tag), tag);
                        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
                        fragment.setArguments(args);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.relLayoutParent, fragment);
                        transaction.addToBackStack(getString(R.string.view_post_fragment));
                        transaction.commit();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int MAIN_FRAGMENT = 0;

    private Context mContext = MainActivity.this;

    private ImageView createPost;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: starting.");

        mViewPager = (ViewPager) findViewById(R.id.container);
        mFrameLayout = (FrameLayout) findViewById(R.id.main_container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);

        //add icon to navigate to CreatePostActivity
        ImageView createPost = (ImageView) findViewById(R.id.createPost);
        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to CreatePostActivity");
                Intent intent = new Intent(mContext, CreatePostActivity.class);
                startActivity(intent);
            }
        });

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
    }

    public void onCommentThreadSelectedListener(Post post) {
        Log.d(TAG, "OnCommentThreadSelectedListener: selected a post thread");

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    public void hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout() {
        Log.d(TAG, "showLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }

    /**
     * Responsible for adding the top tab: Home
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainFragment());
        //adapter.addFragment(new CreatePostFragment());

        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_createpost);
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
    ---------------------------------------firebase------------------------------------------------
     */

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        Log.d(TAG, "checkCurrentUser: user: " + user);
        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

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
        mViewPager.setCurrentItem(MAIN_FRAGMENT);
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



}
