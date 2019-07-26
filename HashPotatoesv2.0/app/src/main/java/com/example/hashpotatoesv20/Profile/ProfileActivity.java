package com.example.hashpotatoesv20.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hashpotatoesv20.Login.LoginActivity;
import com.example.hashpotatoesv20.Main.MainFragment;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.EditPostFragment;
import com.example.hashpotatoesv20.Utils.MainfeedListAdapter;
import com.example.hashpotatoesv20.Utils.ProfileListAdapter;
import com.example.hashpotatoesv20.Utils.UniversalImageLoader;
import com.example.hashpotatoesv20.Utils.ViewCommentsFragment;
import com.example.hashpotatoesv20.Utils.ViewPostFragment;
import com.example.hashpotatoesv20.Utils.ViewProfileFragment;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.onListPostSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewTagFragment.onListPostSelectedListener,
        ViewProfileFragment.onListPostSelectedListener,
        ProfileListAdapter.OnAdapterItemClickListener,
        MainFragment.onListPostSelectedListener,
        MainfeedListAdapter.OnTagSelectedListener{
    
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 3;

    @Override
    public void onClickImage(Post post, int activity_number) {
        Log.d(TAG, "onPostSelected: selected a post from listview " + post.getTag_list());

        EditPostFragment fragment = new EditPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.edit_post_fragment));
        transaction.commit();
    }

    @Override
    public void onViewClicked(Post post, int activity_number) {
        Log.d(TAG, "onPostSelected: selected a post from listview " + post.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post), post);
        args.putInt(getString(R.string.activity_number), ACTIVITY_NUM);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    public void onCommentThreadSelectedListener(Post post){
        Log.d(TAG, "OnCommentThreadSelectedListener: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.post),post);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
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
        transaction.replace(R.id.container, fragment);
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
                        transaction.replace(R.id.container, fragment);
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

    private Context mContext = ProfileActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        init();

    }

    private void init() {
        Log.d(TAG, "init: inflating" + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "init: searching for user object attached as intent extra");
            if (intent.hasExtra(getString(R.string.intent_user))) {
                Log.d(TAG, "init: inflating view profile");
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_user),
                        intent.getParcelableExtra(getString(R.string.intent_user)));
                fragment.setArguments(args);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment));
                transaction.commit();
            }
            else if (intent.hasExtra(getString(R.string.intent_tag))) {
                ViewTagFragment fragment = new ViewTagFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.intent_tag),
                        intent.getParcelableExtra(getString(R.string.intent_tag)));
                fragment.setArguments(args);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_tag_fragment));
                transaction.commit();

            }
            else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "init: inflating profile");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
    }

}
