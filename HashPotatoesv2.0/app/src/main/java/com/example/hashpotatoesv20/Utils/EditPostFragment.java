package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Profile.ProfileActivity;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.dialogs.ConfirmDeleteDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditPostFragment extends Fragment implements
        ConfirmDeleteDialog.OnConfirmDeleteListener {

    @Override
    public void onConfirmDelete() {
        deletePost();
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);

        getActivity().finish();
        Toast.makeText(getActivity(),"Post deleted", Toast.LENGTH_SHORT).show();
    }

    private static final String TAG = "EditPostActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //vars
    private ImageView mDelete;
    private EditText mDiscussion;
    private SwitchCompat mAnonymity;
    private TextView tvAnon;
    private Button mCancel, mFinish;
    private Post mPost;

    //constants
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private Context mContext = getActivity();

    @Nullable
    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editpost, container,false);

        mCancel = (Button) view.findViewById(R.id.btn_cancel);
        mFinish = (Button) view.findViewById(R.id.btn_finish);
        mDiscussion = (EditText) view.findViewById(R.id.edit_discussion);
        mAnonymity = (SwitchCompat) view.findViewById(R.id.edit_anonymity);
        tvAnon = (TextView) view.findViewById(R.id.edit_tvAnon);
        mDelete = (ImageView) view.findViewById(R.id.btn_delete);

        mPost = getPostFromBundle();
        setupFirebaseAuth();

        mDiscussion.setText(mPost.getDiscussion());
        tvAnon.setText(mPost.getAnonymity());
        if (mPost.getAnonymity().equals( "Anonymous")){
            mAnonymity.setOnCheckedChangeListener(null);
            mAnonymity.setChecked(true);
            mAnonymity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){

                    }
                }
            });
        }

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

        try {
            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: closing fragment, back to previous screen");
                    getFragmentManager().popBackStack();
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Deleting post.");
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
                dialog.show(getFragmentManager(),getString(R.string.confirm_delete_dialog));
                dialog.setTargetFragment(EditPostFragment.this,1);
            }
        });

        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: editing post.");
                Toast.makeText(getActivity(),"Attempting to edit post", Toast.LENGTH_SHORT).show();

                updateDiscussion(mDiscussion.getText().toString());
                updateAnonymity(tvAnon.getText().toString());
                getFragmentManager().popBackStack();

            }
        });

        return view;
    }

    private void deletePost(){
        Log.d(TAG, "deletePost: deleting: " + mPost.getPost_id());
        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .removeValue();
        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id())
                .child(mPost.getPost_id())
                .removeValue();
        ArrayList<String> tagIDList = mPost.getTag_list();
        for (int i = 0; i < tagIDList.size(); i++) {
            myRef.child(getString(R.string.dbname_tag_post))
                    .child(tagIDList.get(i))
                    .child(mPost.getPost_id())
                    .removeValue();
        }
    }

    private void updateDiscussion(String newDiscussion){
        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_discussion))
                .setValue(newDiscussion);
        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id())
                .child(mPost.getPost_id())
                .child(getString(R.string.field_discussion))
                .setValue(newDiscussion);
        ArrayList<String> tagIDList = mPost.getTag_list();
        for (int i = 0; i < tagIDList.size(); i++) {
            myRef.child(getString(R.string.dbname_tag_post))
                    .child(tagIDList.get(i))
                    .child(mPost.getPost_id())
                    .child(getString(R.string.field_discussion))
                    .setValue(newDiscussion);
        }
    }

    private void updateAnonymity(String newAnonymity){
        myRef.child(getString(R.string.dbname_posts))
                .child(mPost.getPost_id())
                .child(getString(R.string.field_anonymity))
                .setValue(newAnonymity);
        myRef.child(getString(R.string.dbname_user_posts))
                .child(mPost.getUser_id())
                .child(mPost.getPost_id())
                .child(getString(R.string.field_anonymity))
                .setValue(newAnonymity);
        try {
            ArrayList<String> tagIDList = mPost.getTag_list();
            for (int i = 0; i < tagIDList.size(); i++) {
                myRef.child(mContext.getString(R.string.dbname_tag_post))
                        .child(tagIDList.get(i))
                        .child(mPost.getPost_id())
                        .child(mContext.getString(R.string.field_anonymity))
                        .setValue(newAnonymity);
            }
        }
        catch (NullPointerException e) {
            Log.e(TAG, "addNewLike: NullPointerException: " + e.getMessage());
        }
    }

    /**
     * retrieve the post from the incoming bundle from ProfileActivity interface
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
     * Verify all the permissions passed to the array
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");
        ActivityCompat.requestPermissions(
                getActivity(),
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

        int permissionRequest = ActivityCompat.checkSelfPermission(getActivity(), permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: Permission is not granted for: " + permission);
            return false;
        }
        Log.d(TAG, "checkPermissions: Permission is granted for: " + permission);
        return true;
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
