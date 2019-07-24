package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Profile.AccountSettingsActivity;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.example.hashpotatoesv20.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewTagFragment extends Fragment {
    private static final String TAG = "ViewTagFragment";

    private static final int ACTIVITY_NUM = 1;

    public interface onListPostSelectedListener {
        void onPostSelected(Post post, int activity_number);
    }
    onListPostSelectedListener mOnListPostSelectedListener;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private TextView mTagName, mTagDesc, mPostNum, mPost, mTagNameBar, tvLocked;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;
    private ImageView mPrivate, mPublic, profileMenu, mLocked, mBackArrow;
    private BottomNavigationViewEx bottomNavigationView;
    private ListView listView;
    private Button mFollow, mUnfollow;

    //vars
    private Tag mTag;
    private Context mContext;
    private int mPostsCount = 0;
    private User mCurrentUser;
    private String mNotifString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_tag, container,false);
        mTagName = (TextView) view.findViewById(R.id.tag_name);
        mTagDesc = (TextView) view.findViewById(R.id.tag_desc);
        mPostNum = (TextView) view.findViewById(R.id.tvNumPost);
        mPost = (TextView) view.findViewById(R.id.tvPost);
        mTagNameBar = (TextView) view.findViewById(R.id.username);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        listView = (ListView) view.findViewById(R.id.listView);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        mPrivate = (ImageView) view.findViewById(R.id.ivPrivate);
        mPublic = (ImageView) view.findViewById(R.id.ivPublic);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        mFollow = (Button) view.findViewById(R.id.btn_follow);
        mUnfollow = (Button) view.findViewById(R.id.btn_unfollow);
        mLocked = (ImageView) view.findViewById(R.id.locked);
        tvLocked = (TextView) view.findViewById(R.id.tvLocked);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());

        try{
            mTag = getTagFromBundle();
            Tag tags = mTag;
            setWidgets(tags);
        }
        catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            //getActivity().getSupportFragmentManager().popBackStack();
        }

        setupListView();
        setupBottomNavigationView();
        setupFirebaseAuth();
        setupToolbar();

        isFollowing();
        getPostsCount();
        getCurrentUser();

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().finish();
            }
        });

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTag.getPrivacy().equals("Private")) {
                    //Send notification to tag owner
                    mNotifString = mCurrentUser.getUsername() + "has requested to follow your tag: " + mTag.getTag_name();
                    mFirebaseMethods.addNotificationToDatabase(mTag.getOwner_id(), mNotifString, "", mTag.getTag_id(), mCurrentUser.getUser_id());
                    //Inform user that a request has been sent
                    Toast.makeText(mContext,"Your request to follow this tag has been sent to the owner.", Toast.LENGTH_SHORT).show();

                } else {
                    //if public
                    Log.d(TAG, "onClick: now following: " + mTag.getTag_name());

                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_tag_followers))
                            .child(mTag.getTag_id())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(FirebaseAuth.getInstance().getCurrentUser());

                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_user_following))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mTag.getTag_id())
                            .setValue(mTag);
                    setFollowing();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference.child(mContext.getString(R.string.dbname_tags))
                            .child(mTag.getTag_id());
                    Log.d(TAG, "setWidgets: checking privacy");
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (ds.getValue().equals("Private")) {
                                    //send notification to owner

                                    mLocked.setVisibility(View.GONE);
                                    tvLocked.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowing: " + mTag.getTag_name());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_tag_followers))
                        .child(mTag.getTag_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_user_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mTag.getTag_id())
                        .removeValue();
                setUnfollowing();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference.child(mContext.getString(R.string.dbname_tags))
                        .child(mTag.getTag_id());
                Log.d(TAG, "setWidgets: checking privacy");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.getValue().equals("Private")) {
                                mLocked.setVisibility(View.VISIBLE);
                                tvLocked.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnListPostSelectedListener = (onListPostSelectedListener) getActivity();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void getCurrentUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setWidgets(final Tag tag) {
        String tag_name = "#" + tag.getTag_name();
        mTagName.setText(tag_name);
        mTagNameBar.setText(tag_name);
        mTagDesc.setText(tag.getTag_description());
        String privacy = tag.getPrivacy();
        if (privacy.equals("Private")) {
            mPrivate.setVisibility(View.VISIBLE);
            mPublic.setVisibility(View.INVISIBLE);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_tags))
                    .child(tag.getTag_id());
            Log.d(TAG, "setWidgets: checking privacy");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getValue().equals("Private")) {

                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference();
                            Query query1 = ref1.child(mContext.getString(R.string.dbname_tag_followers))
                                    .child(tag.getTag_id())
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        Log.d(TAG, "onDataChange: ds tagfollow: " + ds.getValue());
                                        if (!ds.getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                            mLocked.setVisibility(View.VISIBLE);
                                            tvLocked.setVisibility(View.VISIBLE);
                                            listView.setVisibility(View.GONE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            mPublic.setVisibility(View.VISIBLE);
            mPrivate.setVisibility(View.INVISIBLE);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private Tag getTagFromBundle(){
        Log.d(TAG, "getTagFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_tag));
        }
        else {
            return null;
        }
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if user is already following this tag");
        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Tag tag = ds.getValue(Tag.class);
                    Log.d(TAG, "onDataChange: tags: " + tag.getTag_id());
                    if (tag.getTag_id().equals(mTag.getTag_id())) {
                        Log.d(TAG, "onDataChange: is following " + tag.getTag_id());
                        setFollowing();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        mPostsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_tag_post))
                .child(mTag.getTag_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found post: " + ds.getValue());
                    mPostsCount++;
                }
                mPostNum.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupListView() {
        Log.d(TAG, "setupListView: Setting up list of user posts.");
        final ArrayList<Post> posts = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_tag_post))
                .child(mTag.getTag_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = "";
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Post post = new Post();
                    Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    post.setDiscussion(objectMap.get(getString(R.string.field_discussion)).toString());
                    post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                    post.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    post.setAnonymity(objectMap.get(getString(R.string.field_anonymity)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    post.setComments(comments);
                    post.setLikes(likesList);
                    posts.add(post);
                }
                //setup list view
                final List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                for (int i = posts.size()-1; i >= 0; i--) {
                    final HashMap<String, String> hm = new HashMap<String, String>();

                    final String postTimestamp = posts.get(i).getDate_created();
                    String timestampDiff = getTimestampDifference(postTimestamp);

                    hm.put(getString(R.string.field_discussion), posts.get(i).getDiscussion());
                    hm.put(getString(R.string.field_date_created), timestampDiff);
                    hm.put(getString(R.string.field_tags), posts.get(i).getTags());
                    aList.add(hm);
                    final String anon = posts.get(i).getAnonymity();

                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
                    Query query2 = reference2
                            .child(getString(R.string.dbname_users_account_settings))
                            .child(posts.get(i).getUser_id());
                    aList.clear();
                    query2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (anon.equals("Anonymous")) {
                                hm.put(getString(R.string.field_username), "Anonymous");
                            }
                            else {
                                hm.put(getString(R.string.field_username), dataSnapshot.getValue(UserAccountSettings.class).getUsername());
                            }
                            aList.add(hm);
                            String[] from = {getString(R.string.field_discussion), getString(R.string.field_date_created),
                                    getString(R.string.field_tags), getString(R.string.field_username)};

                            int[] to = {R.id.post_discussion, R.id.timestamp, R.id.post_tag, R.id.username};

                            MainfeedListAdapter adapter = new MainfeedListAdapter(mContext, R.layout.layout_post_listview, posts);

                            listView.setAdapter(adapter);
                            setListViewHeightBasedOnChildren(listView);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    int actPosition = posts.size() - position - 1;
                                    Log.d(TAG, "onItemClick: position:" + actPosition);
                                    mOnListPostSelectedListener.onPostSelected(posts.get(actPosition), ACTIVITY_NUM);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private static void setListViewHeightBasedOnChildren (ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight() + 25;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        int width = listAdapter.getCount() - 1;

        params.height = totalHeight + (listView.getDividerHeight() * (width));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Returns a string representing the number of days aho the post was made
     * @return
     */
    private String getTimestampDifference(String postTimestamp) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;

        try {
            timestamp = sdf.parse(postTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60)));

            int tsDiff = Integer.parseInt(difference);
            String time = "";
            if (tsDiff == 0) { //less than one minute
                time = "A FEW SECONDS AGO";
                return time;
            }
            else if (tsDiff < 60) { //less than one hour
                if (tsDiff == 1) {
                    time = tsDiff + " MIN AGO";
                }
                else {
                    time = tsDiff + " MINS AGO";
                }
                return time;
            }
            else if (tsDiff < 1440) { //less than one day
                tsDiff = tsDiff / 60;
                if (tsDiff == 1) {
                    time = tsDiff + " HOUR AGO";
                }
                else {
                    time = tsDiff + " HOURS AGO";
                }
                return time;
            }
            else {
                tsDiff = tsDiff / 60/ 24;
                if (tsDiff == 1) {
                    time = tsDiff + " DAY AGO";
                }
                else {
                    time = tsDiff + " DAYS AGO";
                }
                return time;
            }
        }
        catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
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
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(),bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this tag");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        if (mTag.getPrivacy().equals("Private")) {
            mLocked.setVisibility(View.GONE);
            tvLocked.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void setUnfollowing() {
        Log.d(TAG, "setUnfollowing: updating UI for following this tag");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        if (mTag.getPrivacy().equals("Private")) {
            mLocked.setVisibility(View.VISIBLE);
            tvLocked.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
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
