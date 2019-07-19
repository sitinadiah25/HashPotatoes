package com.example.hashpotatoesv20.Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.FirebaseMethods;
import com.example.hashpotatoesv20.Utils.Permissions;
import com.example.hashpotatoesv20.Utils.SectionsStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private EditText mDiscussion, mTag;
    private SwitchCompat mAnonymity;
    private TextView tvAnon, chosenTag;
    private ListView listView;
    private RelativeLayout mParent;

    //vars
    private List<String> mTagList;
    private String taglist;
    private String tags;

    //constants
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = CreatePostActivity.this;

    @Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_createpost, container,false);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createpost);
        Log.d(TAG, "onCreate: CreatePost Activity started.");
        mDiscussion = (EditText) findViewById(R.id.discussion);
        mAnonymity = (SwitchCompat) findViewById(R.id.anonymity);
        mTag = (EditText) findViewById(R.id.searchTag);
        tvAnon = (TextView) findViewById(R.id.tvAnon);
        listView = (ListView) findViewById(R.id.listView);
        chosenTag = (TextView) findViewById(R.id.chosenTag);
        mParent = (RelativeLayout) findViewById(R.id.parent_container);
        chosenTag.setText("");
        taglist = "";
        tags = "";
        tvAnon.setText("Public");
        mFirebaseMethods = new FirebaseMethods(CreatePostActivity.this);

        setupFirebaseAuth();

        mAnonymity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Anonymity switch checked");
                if (mAnonymity.isChecked()) {
                    Log.d(TAG, "onClick: Anonymous Post.");
                    tvAnon.setText("Anonymous");
                }
                else {
                    Log.d(TAG, "onClick: Public Post.");
                    tvAnon.setText("Public");
                }
            }
        });

        Button cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing activity, to back to previous screen");
                finish();
            }
        });

        Button share = (Button) findViewById(R.id.btn_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: sharing post.");

                //check if tags exists

                //upload post to firebase
                Toast.makeText(CreatePostActivity.this, "Attempting to upload post", Toast.LENGTH_SHORT).show();

                String discussion = mDiscussion.getText().toString();
                String sAnonymity = tvAnon.getText().toString();

                mFirebaseMethods.uploadPost(discussion, tags, sAnonymity);

                Intent intent = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intent);
            }
        });

        setupUI(mParent);
        tagListener();

        /*
        if (checkPermissionsArray(Permissions.PERMISSIONS)) {

        }
        else {
            verifyPermissions(Permissions.PERMISSIONS);
        }*/
    }

    private void tagListener() {
        mTagList = new ArrayList<>();
        mTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mTag.getText().toString();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "searchForMatch: searching for: " + keyword);
        mTagList.clear();
        updateTagList();

        if (keyword.isEmpty()) {

        }
        else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_tags))
                    .orderByChild(getString(R.string.field_tag_name))
                    .startAt(keyword)
                    .endAt(keyword+"\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: ds value: " + singleSnapshot.getValue());
                        Map<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        Log.d(TAG, "onDataChange: tag name: " + objectMap.get(mContext.getString(R.string.field_tag_name)).toString());
                        mTagList.add(objectMap.get(mContext.getString(R.string.field_tag_name)).toString());
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
        Log.d(TAG, "updateTagList: updating listview");
        listView.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mTagList);
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: user clicked on tag: " + mTagList.get(position));
                String text = "#" + mTagList.get(position) + " ";
                tags = tags + mTagList.get(position) + " ";
                taglist = taglist + text;
                Log.d(TAG, "onItemClick: text: " + taglist);
                mTag.setText("");
                chosenTag.setText(taglist);
                chosenTag.setVisibility(View.VISIBLE);
                mTagList.clear();
                clearTagList();
            }
        });
    }

    private void clearTagList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mTagList);
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);
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

    private void setupPost(UserSettings userSettings) {
        UserAccountSettings settings = userSettings.getSettings();

        String username = settings.getUsername();
    }

    //hide keyboard if user touches view
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(CreatePostActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    //hide keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    private String getUsername(UserSettings userSettings) {
        UserAccountSettings settings = userSettings.getSettings();
        return settings.getUsername();
    }

    /**
     * Verify all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");
        ActivityCompat.requestPermissions(
                CreatePostActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check single permission if it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(CreatePostActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: Permission is not granted for: " + permission);
            return false;
        }
        Log.d(TAG, "checkPermissions: Permission is granted for: " + permission);
        return true;
    }

    private void someMethod() {
        //Properties to Posts (discussion, date, tag, anonymity)

    }

    /*
    ---------------------------------------firebase------------------------------------------------
    */

    /**
     * Setup the firebase auth object
     **/
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

                setupPost(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve user information from the database

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
