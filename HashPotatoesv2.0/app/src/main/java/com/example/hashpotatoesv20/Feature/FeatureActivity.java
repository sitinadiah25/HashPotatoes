package com.example.hashpotatoesv20.Feature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.MainfeedListAdapter;
import com.example.hashpotatoesv20.Utils.TagListAdapter;
import com.example.hashpotatoesv20.Utils.UserListAdapter;
import com.example.hashpotatoesv20.Utils.ViewTagFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureActivity extends AppCompatActivity implements MainfeedListAdapter.OnTagSelectedListener{

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
                        transaction.replace(R.id.relLayout1, fragment);
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

    private static final String TAG = "FeatureActivity";
    private static final int ACTIVITY_NUM = 1;

    private Context mContext = FeatureActivity.this;

    //widgets
    private EditText mSearchParam;
    private ListView mListViewUser;
    private ListView mListViewTag;
    private ListView mListViewNew;

    //Vars
    private List<User> mUserList;
    private List<Tag> mTagList;
    private List<Post> mPosts;
    private ArrayList<Post> mPaginatedPosts;
    private MainfeedListAdapter mAdapter;
    private UserListAdapter mUAdapter;
    private TagListAdapter mTAdapter;
    private int mResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListViewUser = (ListView) findViewById(R.id.search_listview);
        mListViewTag = (ListView) findViewById(R.id.search_listview_tag);
        mListViewNew = (ListView) findViewById(R.id.search_listview_new);
        mPosts = new ArrayList<>();
        Log.d(TAG, "onCreate: started.");

        hideSoftKeyboard();
        setupBottomNavigationView();
        getPost();
        initTextListener();
    }

    private void getPost() {
        Log.d(TAG, "getPosts: getting posts");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_posts));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    final Post post = new Post();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    post.setDiscussion(objectMap.get(getString(R.string.field_discussion)).toString());
                    post.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    post.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    post.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                    post.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    post.setAnonymity(objectMap.get(getString(R.string.field_anonymity)).toString());

                    if (post.getAnonymity().equals("Public")) {
                        ArrayList<String> tagIDList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 : singleSnapshot
                                .child(getString(R.string.field_tag_list)).getChildren()) {
                            tagIDList.add(dataSnapshot1.getValue().toString());
                        }

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }

                        //find the one with comments pls lol
                        List<Comment> commentList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot1 :
                                singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dataSnapshot1.getValue(Comment.class).getUser_id());
                            comment.setComment(dataSnapshot1.getValue(Comment.class).getComment());
                            comment.setDate_created(dataSnapshot1.getValue(Comment.class).getDate_created());
                        }

                        post.setTag_list(tagIDList);
                        post.setComments(commentList);
                        post.setLikes(likesList);
                        //Log.d(TAG, "onDataChange: post: " + post.toString());

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
                        Query query1 = reference1.child(getString(R.string.dbname_user_following))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean is_following = false;
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    for (int i = 0; i < post.getTag_list().size(); i++) {
                                        if (singleSnapshot.child(mContext.getString(R.string.field_tag_id)).getValue().toString().equals(post.getTag_list().get(i))) {
                                            is_following = true;
                                        }
                                    }
                                }

                                if (!is_following) {
                                    mPosts.add(post);
                                }
                                displayPosts();
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

    private void displayPosts(){
        Log.d(TAG, "displayPosts: displaying posts.");
        mPaginatedPosts = new ArrayList<>();
        if (mPosts != null) {
            try {
                Collections.sort(mPosts, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iteration = mPosts.size();
                Log.d(TAG, "displayPosts: Checking post size: " + mPosts.size());

                if (iteration > 10){
                    iteration = 10;
                }
                mResults = 10;
                for (int i = 0; i < iteration; i ++){
                    mPaginatedPosts.add(mPosts.get(i));
                }
                mAdapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_listitem, mPaginatedPosts);
                mListViewNew.setAdapter(mAdapter);
                setListViewHeightBasedOnChildren(mListViewNew);

                mListViewNew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int actPosition = mPaginatedPosts.size() - position - 1;
                        Log.d(TAG, "onItemClick: position:" + actPosition);
                        //mOnListPostSelectedListener.onPostSelected(mPaginatedPosts.get(position), ACTIVITY_NUM);
                    }
                });

            } catch (NullPointerException e) {
                Log.e(TAG, "displayPosts: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPosts: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
    }

    private void initTextListener(){
        Log.d(TAG,"initTextListener: initializing");

        mUserList = new ArrayList<>();
        mTagList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString();
                searchForMatch(text);
                mListViewNew.setVisibility(View.GONE);
                if (text.isEmpty()) {
                    mUserList.clear();
                    mTagList.clear();
                    updateUserList();
                    updateTagList();
                    hideSoftKeyboard();
                    mListViewNew.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        mTagList.clear();
        updateUserList();
        updateTagList();
        if (keyword.length() != 0 && keyword.charAt(0) == '#') {
            keyword = keyword.substring(1);
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
            Query query2 = reference2.child(getString(R.string.dbname_tags))
                    .orderByChild(getString(R.string.field_tag_name))
                    .startAt(keyword)
                    .endAt(keyword+"\uf8ff");
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        Tag tag = new Tag();
                        tag.setPrivacy(objectMap.get(mContext.getString(R.string.field_privacy)).toString());
                        tag.setTag_name(objectMap.get(mContext.getString(R.string.field_tag_name)).toString());
                        tag.setTag_id(objectMap.get(mContext.getString(R.string.field_tag_id)).toString());
                        tag.setTag_description(objectMap.get(mContext.getString(R.string.field_tag_description)).toString());
                        tag.setOwner_id(objectMap.get(mContext.getString(R.string.field_owner_id)).toString());

                        mTagList.add(tag);
                        //update the tags list view
                        updateTagList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else if (keyword.length() != 0) {
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
            Query query1 = reference1.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .startAt(keyword)
                    .endAt(keyword+"\uf8ff");
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));
                        updateUserList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
            Query query2 = reference2.child(getString(R.string.dbname_tags))
                    .orderByChild(getString(R.string.field_tag_name))
                    .startAt(keyword)
                    .endAt(keyword+"\uf8ff");
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        Tag tag = new Tag();
                        tag.setPrivacy(objectMap.get(mContext.getString(R.string.field_privacy)).toString());
                        tag.setTag_name(objectMap.get(mContext.getString(R.string.field_tag_name)).toString());
                        tag.setTag_id(objectMap.get(mContext.getString(R.string.field_tag_id)).toString());
                        tag.setTag_description(objectMap.get(mContext.getString(R.string.field_tag_description)).toString());
                        tag.setOwner_id(objectMap.get(mContext.getString(R.string.field_owner_id)).toString());

                        mTagList.add(tag);
                        //update the tags list view
                        updateTagList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateTagList() {
        Log.d(TAG, "updateTagList: updating tags list");

        mTAdapter = new TagListAdapter(FeatureActivity.this, R.layout.layout_tag_listitem, mTagList);

        mTAdapter.notifyDataSetChanged();
        mListViewTag.setAdapter(mTAdapter);
        setListViewHeightBasedOnChildren(mListViewTag);
        mListViewTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " +  mTagList.get(position).toString());
                //navigate to profile activity
                Intent intent = new Intent(FeatureActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.feature_activity));
                intent.putExtra(getString(R.string.intent_tag), mTagList.get(position));
                startActivity(intent);
            }
        });
    }

    private void updateUserList(){
        Log.d(TAG, "updateUserList: updating users list");

        mUAdapter = new UserListAdapter(FeatureActivity.this, R.layout.layout_user_listitem, mUserList);

        mUAdapter.notifyDataSetChanged();
        mListViewUser.setAdapter(mUAdapter);
        setListViewHeightBasedOnChildren(mListViewUser);
        mListViewUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " +  mUserList.get(position).toString());
                //navigate to profile activity
                Intent intent = new Intent(FeatureActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.feature_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);
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

    private void hideSoftKeyboard(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
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
}
