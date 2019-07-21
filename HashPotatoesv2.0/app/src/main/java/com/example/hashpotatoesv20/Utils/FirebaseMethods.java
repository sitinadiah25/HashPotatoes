package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.hashpotatoesv20.Models.Notification;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.Models.UserSettings;
import com.example.hashpotatoesv20.Profile.AccountSettingsActivity;
import com.example.hashpotatoesv20.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;
    private String username;

    private Context mContext;
    private double mPhotoUploadProgress = 0;

    private UserSettings user;
    private User cUser;

    public FirebaseMethods(Context context){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadProfilePhoto(String photoType, String imgURL, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");

        FilePaths filePaths = new FilePaths();

        if (bm == null) {
            //convert image url to bitmap
            bm = ImageManager.getBitmap(imgURL);
        }

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageReference = mStorageReference
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

        UploadTask uploadTask = null;
        uploadTask = storageReference.putBytes(bytes);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String photoLink = uri.toString();
                        Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                        //insert into 'user_account_settings' node
                        setProfilePhoto(photoLink);

                        ((AccountSettingsActivity)mContext).setViewPager(
                                (((AccountSettingsActivity)mContext).pagerAdapter
                                        .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)))
                        );
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Photo upload failed.");
                Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                if(progress - 15 > mPhotoUploadProgress){
                    Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    mPhotoUploadProgress = progress;
                }

                Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
            }
        });
    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    public void updateUserAccountSettings(String displayname, String description, String website, String year, String major){
        if(displayname != null){
            myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_displayname))
                    .setValue(displayname);
        }

        if(description != null){
            myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(website != null){
            myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(year != null){
            myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_year))
                    .setValue(year);
        }

        if(major != null){
            myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_major))
                    .setValue(major);
        }

    }


    /**
     * update the username in the 'users' and 'users account setting' node in firebase
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the user's node in firebase
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    public void createTag(final String tagName, final String tagDescription, final String privacy) {
        //check if tag name exists
        Log.d(TAG, "createTag: checking if tag name already exists.");

        //add tag to database
        addTagToDatabase(tagName, tagDescription, privacy);
    }

    public void uploadPost(String discussion, String tags, String anonymity) {

        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference refTemp = FirebaseDatabase.getInstance().getReference("users").child(user_id);

        addPostToDatabase(discussion, tags, anonymity);
    }

    /**
     * Register a new email and password to Firebase authentication
     * @param email
     * @param password
     * @param username
     */

    public void registerNewEmail(final String email, String password, String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //send verification email
                            sendVerificationEmail();

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG,"onComplete: Authstate changed" + userID);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            }
                            else {
                                Toast.makeText(mContext, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
     * Add post to the database
     * @param discussion
     * @param tags
     * @param anonymity
     */
    private void addPostToDatabase(String discussion, String tags, String anonymity) {
        Log.d(TAG, "addPostToDatabase: adding post to database.");

        final String newPostKey = myRef.child(mContext.getString(R.string.dbname_posts)).push().getKey();

        //tokenizing tag string to get the different tags
        StringTokenizer st = new StringTokenizer(tags);
        final ArrayList<String> tagList = new ArrayList<>();
        Log.d(TAG, "addPostToDatabase: tokens: " + st.countTokens());
        if (st.countTokens() == 1) {
            tags = tags.substring(0, tags.length() - 1);
            tagList.add(tags);
        }
        else {
            while (st.hasMoreElements()) {
                tagList.add(st.nextElement().toString());
            }
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Post post = new Post();
        post.setDiscussion(discussion);
        post.setTags(tags);
        post.setAnonymity(anonymity);
        post.setDate_created(getTimestamp());
        post.setPost_id(newPostKey);
        post.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //get list of tag ids
        final ArrayList<String> tagIDList = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            final String currTag = tagList.get(i);
            Query query1 = reference.child(mContext.getString(R.string.dbname_tags))
                    .orderByChild(mContext.getString(R.string.field_tag_id));
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: child value n: " + currTag + " " + ds.getValue(Tag.class).getTag_name());
                        if (currTag.equals(ds.getValue(Tag.class).getTag_name())) {
                            Log.d(TAG, "onDataChange: success get tag");
                            tagIDList.add(ds.getValue(Tag.class).getTag_id());
                        }
                    }
                    post.setTag_list(tagIDList);
                    Log.d(TAG, "onDataChange: tagidlist size: " + tagIDList.size());
                    //insert into database
                    for (int i = 0; i < tagIDList.size(); i++) {
                        final String currTagID = tagIDList.get(i);
                        Log.d(TAG, "addPostToDatabase: tag id: " + currTagID);

                        myRef.child(mContext.getString(R.string.dbname_tag_post))
                                .child(currTagID)
                                .child(newPostKey)
                                .setValue(post);
                    }
                    myRef.child(mContext.getString(R.string.dbname_posts)).child(newPostKey).setValue(post);
                    myRef.child(mContext.getString(R.string.dbname_user_posts))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(newPostKey).setValue(post);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        Log.d(TAG, "addPostToDatabase: added post to database.");
    }

    public void addTagToDatabase(String tag_name, String tag_desc, String privacy) {
        Log.d(TAG, "addPostToDatabase: adding post to database.");

        String newTagKey = myRef.child(mContext.getString(R.string.dbname_tags)).push().getKey();

        Tag tag = new Tag();

        tag.setOwner_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        tag.setTag_name(tag_name);
        tag.setTag_description(tag_desc);
        tag.setTag_id(newTagKey);
        tag.setPrivacy(privacy);
        ArrayList<User> whitelist = new ArrayList<>();
        ArrayList<Post> post_id = new ArrayList<>();
        tag.setPost_ids(post_id);
        tag.setWhitelist(whitelist);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_tags)).child(newTagKey).setValue(tag);
        myRef.child(mContext.getString(R.string.dbname_user_tags))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newTagKey)
                .setValue(tag);
        myRef.child(mContext.getString(R.string.dbname_tag_followers))
                .child(newTagKey)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(FirebaseAuth.getInstance().getCurrentUser());
        myRef.child(mContext.getString(R.string.dbname_user_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newTagKey)
                .setValue(tag);

        Log.d(TAG, "addTagToDatabase: added tag to database.");
    }

    /**
     * Add information to the users nodes
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     * @param major
     */
    public void addNewUser(String email, String username, String description,
                           String website, String profile_photo, String major) {
        username = username.toLowerCase();
        User user = new User(userID, StringManipulation.condenseUsername(username), email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings();
        settings.setDescription(description);
        settings.setDisplay_name(username);
        settings.setMajor(major);
        settings.setPosts(0);
        settings.setProfile_photo(profile_photo);
        settings.setUsername(StringManipulation.condenseUsername(username));
        settings.setWebsite(website);
        settings.setYear("");
        settings.setUser_id(userID);

        myRef.child(mContext.getString(R.string.dbname_users_account_settings))
                .child(userID)
                .setValue(settings);
    }

    public User getCurrentUser() {
        cUser = new User();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: ds: " + ds.getValue());
                    if (ds.getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        cUser = ds.getValue(User.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return cUser;
    }

    /**
     * Retrieves the account settings for the user currently logged in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            //user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setHashtags(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getHashtags()
                    );
                    settings.setMajor(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getMajor()
                    );
                    settings.setYear(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getYear()
                    );
                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                }
                catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }


            }

            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUser: datasnapshot: " + ds);

                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
            }
        }
        return new UserSettings(user, settings);
    }

    /**
     *
     * Add new notif to database
     *
     * @param uid
     * @param notifstring
     * @param postid
     * @param tag
     * @param user_id
     */
    public void addNotificationToDatabase(String uid, String notifstring, String postid, String tag, String user_id){
        Log.d(TAG, "addNotificationtoDatabase: adding notification to database");

        final String newnotifkey = myRef.child(mContext.getString(R.string.dbname_user_notif)).push().getKey();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Notification notification = new Notification();

        notification.setMessage(notifstring);
        notification.setPost_id(postid);
        notification.setTag(tag);
        notification.setViewer_uid(user_id);
        notification.setDate_created(getTimestamp());

        myRef.child(mContext.getString(R.string.dbname_user_notif))
                .child(uid)
                .child(newnotifkey)
                .setValue(notification);
    }


}
