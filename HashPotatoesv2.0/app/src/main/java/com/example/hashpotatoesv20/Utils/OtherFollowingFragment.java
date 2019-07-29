package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
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

import java.util.ArrayList;
import java.util.List;

public class OtherFollowingFragment extends Fragment {

    private static final String TAG = "OtherFollowingFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private TextView mUsername;
    private ListView mListView;
    private ImageView mBackArrow;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    //vars
    private List<Tag> mTagList;
    private TagListAdapter mTAdapter;
    private String username;
    private User mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container,false);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mUsername = (TextView) view.findViewById(R.id.usernameFollowing);
        mListView = (ListView) view.findViewById(R.id.listView);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mContext = (Context) getActivity();
        mTagList = new ArrayList<>();

        try{
            mUser = getUserFromBundle();
            init();
            setupTagList();
        }
        catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            getActivity().getSupportFragmentManager().popBackStack();
        }

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to profile.");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setupBottomNavigationView();
        setupFirebaseAuth();
        return view;
    }

    private void init() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_users_account_settings))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: getting username: ");
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: getting username: " + dataSnapshot
                            .getValue(UserAccountSettings.class).getUsername());
                    username = dataSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
                mUsername.setText(username + " Following");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //setup list of tags followed
    private void setupTagList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_following))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found tag: " +
                            ds.child(getString(R.string.field_tag_id)).getValue());

                    mTagList.add(ds.getValue(Tag.class));
                }
                mTAdapter = new TagListAdapter(getActivity(), R.layout.layout_tag_listitem, mTagList);
                mListView.setAdapter(mTAdapter);
                setListViewHeightBasedOnChildren(mListView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected tag: " +  mTagList.get(position).toString());

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.feature_activity));
                intent.putExtra(getString(R.string.intent_tag), mTagList.get(position));
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
            totalHeight += view.getMeasuredHeight() + 30;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int width = listAdapter.getCount() - 1;
        params.height = totalHeight + (listView.getDividerHeight() * (width));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        }
        else {
            return null;
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
        MenuItem menuItem = menu.getItem(3);
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
