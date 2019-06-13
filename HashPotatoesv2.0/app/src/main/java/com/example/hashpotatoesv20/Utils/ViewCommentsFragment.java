package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
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

public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "ViewCommentsFragment";

    public ViewCommentsFragment(){
        super();
        setArguments(new Bundle());
    }
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBack, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Post mPost;
    private ArrayList<Comment> mComments;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBack = (ImageView) view.findViewById(R.id.backArrow);
        mCheckMark = (ImageView) view.findViewById(R.id.confirm_post_comment);
        mComment = (EditText) view.findViewById(R.id.comment);
        mListView = (ListView) view.findViewById(R.id.listView);
        mComments = new ArrayList<Comment>();
        mContext = getActivity();


        try {
            mPost = getPostFromBundle();
            setupFirebaseAuth();
        }
        catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        return view;
    }

    private void setupWidgets(){

        CommentListAdapter adapter = new CommentListAdapter(getActivity(),R.layout.layout_comment,mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(getActivity(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
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

        Log.d(TAG,"Test: " + mPost.getPost_id());

        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                       @Override
                       public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                           Log.d(TAG, "onChildAdded: child added.");
                           Query query = myRef
                                   .child(getString(R.string.dbname_posts))
                                   .orderByChild(getString(R.string.field_post_id))
                                   .equalTo(mPost.getPost_id());
                           query.addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                       Post post = new Post();
                                       Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                       post.setDiscussion(objectMap.get(getString(R.string.field_discussion)).toString());
                                       post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                       post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                       post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                                       post.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                       post.setAnonymity(objectMap.get(getString(R.string.field_anonymity)).toString());

                                       mComments.clear();
                                       Comment firstComment = new Comment();
                                       firstComment.setComment(mPost.getDiscussion());
                                       firstComment.setUser_id(mPost.getUser_id());
                                       firstComment.setDate_created(mPost.getDate_created());

                                       mComments.add(firstComment);

                                       for (DataSnapshot dSnapshot : singleSnapshot
                                               .child(getString(R.string.field_comments)).getChildren()) {
                                           Comment comment = new Comment();
                                           comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                           comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                           comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                           mComments.add(comment);
                                       }

                                       post.setComments(mComments);
                                       mPost = post;
                                       setupWidgets();
                                   }
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError databaseError) {

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