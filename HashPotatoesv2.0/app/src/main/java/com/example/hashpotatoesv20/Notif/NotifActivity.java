package com.example.hashpotatoesv20.Notif;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Notification;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.ViewPostFragment;
import com.example.hashpotatoesv20.dialogs.ConfirmFollowerRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotifActivity extends AppCompatActivity implements
        NotifFragment.onListNotifSelectedListener,
        ConfirmFollowerRequest.OnAllowFollowerListener {

    private static final String TAG = "NotificationActivity";
    private static final int ACTIVITY_NUM = 2;

    private boolean has_followed = false;

    @Override
    public void onNotifSelected(Notification notification, int activity_num) {
        if(!notification.getPost_id().equals("")) {
            Log.d(TAG, "onNotifSelected: going to view post fragment.");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_user_posts))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderByChild(getString(R.string.field_post_id))
                    .equalTo(notification.getPost_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        UserAccountSettings userAccountSetting = new UserAccountSettings();
                        Post post = new Post();

                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        try {
                            post.setDiscussion(objectMap.get(getString(R.string.field_discussion)).toString());
                            post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                            post.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                            post.setAnonymity(objectMap.get(getString(R.string.field_anonymity)).toString());

                            ArrayList<Comment> comments = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_comments)).getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                comments.add(comment);
                            }

                            post.setComments(comments);

                            List<Like> likesList = new ArrayList<Like>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_likes)).getChildren()) {
                                Like like = new Like();
                                like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                likesList.add(like);
                            }
                            post.setLikes(likesList);
                        } catch (NullPointerException e) {
                            Log.e(TAG, "onDataChange: Null Pointer Exception: " + e.getMessage());
                        }

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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Log.d(TAG, "onNotifSelected: starting follower request dialog.");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_tags))
                    .orderByChild(getString(R.string.field_tag_id))
                    .equalTo(notification.getTag());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        Tag tag = new Tag();
                        tag.setOwner_id(objectMap.get(getString(R.string.field_owner_id)).toString());
                        tag.setPrivacy(objectMap.get(getString(R.string.field_privacy)).toString());
                        tag.setTag_description(objectMap.get(getString(R.string.field_tag_description)).toString());
                        tag.setTag_id(objectMap.get(getString(R.string.field_tag_id)).toString());
                        tag.setTag_name(objectMap.get(getString(R.string.field_tag_name)).toString());

                        mTag = tag;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
            Query query2 = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(notification.getViewer_uid());
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        User user = new User();
                        user.setUsername(objectMap.get(getString(R.string.field_username)).toString());
                        user.setEmail(objectMap.get(getString(R.string.field_email)).toString());
                        user.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());

                        mFollower_user = user;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Log.d(TAG, "onNotifSelected: get tag_id: " + notification.getTag());

            DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference();
            Query query3 = reference.child(getString(R.string.dbname_tag_followers))
                    .child(notification.getTag())
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(notification.getViewer_uid());
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    has_followed = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if(!has_followed){
                ConfirmFollowerRequest dialog = new ConfirmFollowerRequest();
                dialog.show(getSupportFragmentManager(),getString(R.string.confirm_follower_request));
            }else{
                Toast.makeText(mContext,"User is following this tag.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onAllowFollower(){
        //add to database
        Log.d(TAG, "onAllowFollower: adding follower to tag");
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_tag_followers))
                .child(mTag.getTag_id())
                .child(mFollower_user.getUser_id())
                .setValue(mFollower_user);

        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_user_following))
                .child(mFollower_user.getUser_id())
                .child(mTag.getTag_id())
                .setValue(mTag);

        //close dialog
        getFragmentManager().popBackStack();
        Toast.makeText(mContext,"User now following tag.", Toast.LENGTH_SHORT).show();
    }

    private Context mContext = NotifActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //Follower request
    private Tag mTag;
    private User mFollower_user;

    @Nullable
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);
        Log.d(TAG, "onCreate: started.");

        NotifFragment fragment = new NotifFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_notif, fragment);
        transaction.addToBackStack(getString(R.string.notification_fragment));
        transaction.commit();

        setupBottomNavigationView();

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
