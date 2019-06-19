package com.example.hashpotatoesv20.Feature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.BottomNavigationViewHelper;
import com.example.hashpotatoesv20.Utils.TagListAdapter;
import com.example.hashpotatoesv20.Utils.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeatureActivity extends AppCompatActivity {
    private static final String TAG = "FeatureActivity";
    private static final int ACTIVITY_NUM = 1;

    private Context mContext = FeatureActivity.this;

    //widgets
    private EditText mSearchParam;
    private ListView mListViewUser;
    private ListView mListViewTag;

    //Vars
    private List<User> mUserList;
    private List<Tag> mTagList;
    private UserListAdapter mUAdapter;
    private TagListAdapter mTAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListViewUser = (ListView) findViewById(R.id.search_listview);
        mListViewTag = (ListView) findViewById(R.id.search_listview_tag);
        Log.d(TAG, "onCreate: started.");

        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
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
            }
        });
    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        mTagList.clear();
        //update the users list
        if(keyword.length() == 0){

        }else{
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
            Query query1 = reference1.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username)).equalTo(keyword);
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));
                        //update the users list view
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
                    .equalTo(keyword);
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found tag: " + singleSnapshot.getValue(Tag.class).toString());

                        mTagList.add(singleSnapshot.getValue(Tag.class));
                        //update the users list view
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

        mListViewTag.setAdapter(mTAdapter);
        setListViewHeightBasedOnChildren(mListViewTag);
        mListViewTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " +  mTagList.get(position).toString());

                //navigate to profile activity
//                Intent intent = new Intent(FeatureActivity.this, ProfileActivity.class);
//                intent.putExtra(getString(R.string.calling_activity),getString(R.string.feature_activity));
//                intent.putExtra(getString(R.string.intent_tag), mTagList.get(position));
//                startActivity(intent);
            }
        });
    }

    private void updateUserList(){
        Log.d(TAG, "updateUserList: updating users list");

        mUAdapter = new UserListAdapter(FeatureActivity.this, R.layout.layout_user_listitem, mUserList);

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
            totalHeight += view.getMeasuredHeight();
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
