package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelectedListener(Post post);
    }
    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;
    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    private static class ViewHolder{
        String comment, username, timestamp;
    }

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mUsername, mDiscussion, mTimestamp, mLikedBy, mTag;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mCheckmark, mEdit;
    private EditText mCommentText;
    private ListView mListView;

    //variables
    private Post mPost;
    private int mActivityNumber = 0;
    private String photoUsername;
    private String photoUrl;
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private ArrayList<Comment> mComments;
    private Context mContext;
    private User mCurrentUser;
    private String mNotifString="";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartWhite = (ImageView) view.findViewById(R.id.btn_heart_white);
        mHeartRed = (ImageView) view.findViewById(R.id.btn_heart_red);
        mEdit = (ImageView) view.findViewById(R.id.btn_edit);
        mProfileImage = (ImageView) view.findViewById(R.id.profilePhoto);
        mUsername = (TextView) view.findViewById(R.id.username);
        mDiscussion = (TextView) view.findViewById(R.id.post_discussion);
        mTimestamp = (TextView) view.findViewById(R.id.timestamp);
        mLikedBy = (TextView) view.findViewById(R.id.post_likes);
        mTag = (TextView) view.findViewById(R.id.post_tag);
        mCommentText = (EditText) view.findViewById(R.id.add_comment);
        mCheckmark = (ImageView) view.findViewById(R.id.checkmark);
        mListView = (ListView) view.findViewById(R.id.comment_list);
        mContext = getActivity();

        mHeart = new Heart(mHeartWhite,mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(),new GestureListener());

        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().finish();
            }
        });

        try {
            mPost = getPostFromBundle();
            mActivityNumber = getActivityNumFromBundle();
            Log.d(TAG, "onCreateView: Viewing post id: " + mPost.getPost_id());
            getPostDetails();
            getCurrentUser();
        }
        catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(mPost.getUser_id())){
            mEdit.setVisibility(View.VISIBLE);
        }
        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG,"onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }
                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(mCurrentUser.getUsername() + ",")){
                                mLikedByCurrentUser = true;
                            }else{
                                mLikedByCurrentUser = false;
                            }

                            Log.d(TAG, "onDataChange: likedbycurrentuser: " + mLikedByCurrentUser);

                            int length = splitUsers.length;
                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0];
                            }
                            else if (length == 2){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }
                            else if (length == 3){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];
                            }
                            else if (length > 3){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + "others";
                            }
                            Log.d(TAG, "onDataChange: likes string: " + mLikesString);
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e){
            Log.d(TAG, "onDoubleTap:  double tap detected.");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_posts))
                    .child(mPost.getPost_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        String KeyID = singleSnapshot.getKey();

                        //Case 1: already liked
                        if (mLikedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            myRef.child(getString(R.string.dbname_posts))
                                    .child(mPost.getPost_id())
                                    .child(getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            myRef.child(getString(R.string.dbname_user_posts))
                                    .child(mPost.getUser_id())
                                    .child(mPost.getPost_id())
                                    .child(getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            try {
                                ArrayList<String> tagIDList = mPost.getTag_list();
                                for (int i = 0; i < tagIDList.size(); i++) {
                                    myRef.child(mContext.getString(R.string.dbname_tag_post))
                                            .child(tagIDList.get(i))
                                            .child(mPost.getPost_id())
                                            .child(mContext.getString(R.string.field_likes))
                                            .child(KeyID)
                                            .removeValue();
                                }
                            }
                            catch (NullPointerException e) {
                                Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                            }
                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //Case 2: not liked
                        else if (!mLikedByCurrentUser) {
                            addNewLike();
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new Like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id())
                .child(mPost.getPost_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        try {
            ArrayList<String> tagIDList = mPost.getTag_list();
            //Log.d(TAG, "addNewLike: tagIDList: " + tagIDList);
            for (int i = 0; i < tagIDList.size(); i++) {
                myRef.child(mContext.getString(R.string.dbname_tag_post))
                        .child(tagIDList.get(i))
                        .child(mPost.getPost_id())
                        .child(mContext.getString(R.string.field_likes))
                        .child(newLikeID)
                        .setValue(like);
            }
        }
        catch (NullPointerException e) {
            Log.e(TAG, "addNewLike: NullPointerException: " + e.getMessage());
        }
        mHeart.toggleLike();
        getLikesString();

        //notify post owner
        if(!mCurrentUser.equals(FirebaseAuth.getInstance().getCurrentUser().toString())){
            mNotifString = mCurrentUser.getUsername() + " liked your post.";

            mFirebaseMethods.addNotificationToDatabase(mPost.getUser_id(),mNotifString,mPost.getPost_id(),"",mCurrentUser.getUser_id());
        }
    }
    private void getPostDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPost.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setupWidgets() {
        final String postTimestamp = mPost.getDate_created();
        String timestampDiff = getTimestampDifference(postTimestamp);
        mTimestamp.setText(timestampDiff);
        if (mPost.getAnonymity().equals("Anonymous")) {
            mUsername.setText("Anonymous");
            UniversalImageLoader.setImage("", mProfileImage,null,"");
        }
        else {
            mUsername.setText(mUserAccountSettings.getUsername());
            UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage,null,"");
        }
        //Log.d(TAG, "getDiscussion: check discussion: " + mPost.getDiscussion());
        mDiscussion.setText(mPost.getDiscussion());
        mLikedBy.setText(mLikesString);
        //String joinedTags = String.join(" ", tags.);

        String tag = mPost.getTags();
        String[] tokens = tag.split(" ");
        String newTag = "";
        int tokenCount = tokens.length;
        for (int i = 0; i < tokenCount; i++) {
            newTag = newTag + "#" + tokens[i] + " ";
        }
        mTag.setText(newTag);

        //setup list for comments
        setupListView();

        //checkmark for comment
        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommentText.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mCommentText.getText().toString());

                    mCommentText.setText("");
                    closeKeyboard();
                } else {
                    //Toast.makeText(getActivity(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                    closeKeyboard();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to EditPostFragment");
                EditPostFragment fragment = new EditPostFragment();
                Bundle args = new Bundle();
                args.putParcelable(getString(R.string.post), mPost);
                args.putInt(getString(R.string.activity_number), mActivityNumber);
                fragment.setArguments(args);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.edit_post_fragment));
                transaction.commit();
            }
        });
        mEllipses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to ReportPostFragment");
                ReportPostFragment fragment = new ReportPostFragment();
                Bundle args = new Bundle();
                args.putParcelable("POST", mPost);
                args.putInt(getString(R.string.activity_number), mActivityNumber);
                fragment.setArguments(args);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.report_post_fragment));
                transaction.commit();
            }
        });


        if(mLikedByCurrentUser) {
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch:  red heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
        else {
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch:  white heart touch detected.");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }

    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void setupListView() {
        Log.d(TAG, "setupListView: Setting up list of user comments.");
        final ArrayList<ViewHolder> mViewHolder = new ArrayList<>();
        final HashSet<ViewHolder> hashSet = new HashSet<ViewHolder>();
        Log.d(TAG,"Test: " + mPost.getPost_id());

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(mContext.getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_posts))
                                .child(mPost.getPost_id())
                                .child(mContext.getString(R.string.field_comments));

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //mViewHolder.clear();
                                for (final DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Comment comment = singleSnapshot.getValue(Comment.class);
                                    final Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                    Log.d(TAG, "onDataChange: datasnapshot: " + comment.getUser_id());
                                    final ViewHolder viewHolder = new ViewHolder();
                                    mViewHolder.clear();
                                    Query query = reference
                                            .child(getString(R.string.dbname_users))
                                            .orderByChild(getString(R.string.field_user_id))
                                            .equalTo(comment.getUser_id());

                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            //mViewHolder.clear();
                                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                viewHolder.username = (ds.getValue(User.class).getUsername());
                                                viewHolder.comment = objectMap.get(mContext.getString(R.string.field_comment)).toString();
                                                viewHolder.timestamp = objectMap.get(mContext.getString(R.string.field_date_created)).toString();
                                                Log.d(TAG, "onDataChange: timestamp: " + viewHolder.timestamp);
                                                Log.d(TAG, "onDataChange: comment: " + viewHolder.comment);
                                            }
                                            mViewHolder.add(viewHolder);
                                    //setup list view
                                    Log.d(TAG, "onDataChange: mviewholder.size(): " + mViewHolder.size());
                                    double num = Math.sqrt(mViewHolder.size());
                                    List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                                    try {
                                        for (int i = mViewHolder.size()-1; i >= mViewHolder.size()-num; i--) {
                                            HashMap<String, String> hm = new HashMap<String, String>();
                                            Log.d(TAG, "onDataChange: mviewholder.size(): " + mViewHolder.size());
                                            Log.d(TAG, "onDataChange: timestamp: " + mViewHolder.get(i).timestamp);
                                            final String postTimestamp = mViewHolder.get(i).timestamp;
                                            String timestampDiff = getTimestampDifference(postTimestamp);

                                            hm.put(mContext.getString(R.string.field_comments), mViewHolder.get(i).comment);
                                            hm.put(mContext.getString(R.string.field_username), mViewHolder.get(i).username);
                                            hm.put(mContext.getString(R.string.field_date_created), timestampDiff);
                                            aList.add(hm);
                                        }
                                    }
                                    catch (NullPointerException e) {
                                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                                    }

                                    String[] from = {mContext.getString(R.string.field_comments), mContext.getString(R.string.field_username),
                                            mContext.getString(R.string.field_date_created)};

                                    int[] to = {R.id.post_comment, R.id.username, R.id.timestamp};

                                    Log.d(TAG, "onDataChange: viewing post screen");
                                    SimpleAdapter adapter = new SimpleAdapter(mContext, aList, R.layout.layout_comment_listview, from, to);
                                    mListView.setAdapter(adapter);
                                    setListViewHeightBasedOnChildren(mListView);
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

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        int width = listAdapter.getCount() - 1;

        params.height = totalHeight + (listView.getDividerHeight() * (width));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new Comment: " + newComment);

        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id())
                .child(mPost.getPost_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //notify post owner
        //if(!mCurrentUser.toString().equals(FirebaseAuth.getInstance().getCurrentUser().toString())){

        mNotifString = mCurrentUser.getUsername() + " commented on your post: " + comment.getComment();

        mFirebaseMethods.addNotificationToDatabase(mPost.getUser_id(),mNotifString,mPost.getPost_id(),"",mCurrentUser.getUser_id());
        //
    }

    private String getCommmentUserDetails(final String uID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(uID);

        final User user = new User();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    //user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
        return mUserAccountSettings.getUsername();
    }

    /**
     * Get current time (please recheck to ensure that it is Singapore Timezone)
     * @return
     */
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        return sdf.format(new Date());
    }

    /**
     * Returns a string representing the number of days aho the post was made
     * @return
     */
    private String getTimestampDifference(String postTimestamp) {
        //Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        //final String postTimestamp = mPost.getDate_created();
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
                time = tsDiff + " MINS AGO";
                return time;
            }
            else if (tsDiff < 1440) { //less than one day
                tsDiff = tsDiff / 60;
                time = tsDiff + " HOURS AGO";
                return time;
            }
            else { //at least one day
                tsDiff = tsDiff / 60 / 24;
                time = tsDiff + " DAYS AGO";
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
     * retrieve the post from the incoming bundle from profileactivity interface
     * @return
     */
    private Post getPostFromBundle() {
        Log.d(TAG, "getPostFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.post));
        }
        else {
            return null;
        }
    }

    /**
     * retrieve the activity number from the incoming bundle from profileactivity interface
     * @return
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getPostFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        }
        else {
            return 0;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom navigation view");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(),bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
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
                } else {
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
