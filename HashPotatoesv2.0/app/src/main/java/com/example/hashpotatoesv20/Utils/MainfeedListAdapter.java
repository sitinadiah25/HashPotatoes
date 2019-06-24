package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hashpotatoesv20.Main.MainActivity;
import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Like;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.User;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class MainfeedListAdapter extends ArrayAdapter<Post> {

    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public MainfeedListAdapter(Context context, int resource, List<Post> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{
        String likesString;
        TextView username, timeDetails, discussion, tags, likesNum, comment, commentNum;
        ImageView heartRed, heartWhite;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Post post;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.btn_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.btn_heart_white);
            holder.timeDetails = (TextView) convertView.findViewById(R.id.timestamp);
            holder.discussion = (TextView) convertView.findViewById(R.id.post_discussion);
            holder.tags = (TextView) convertView.findViewById(R.id.post_tag);
            holder.comment = (TextView) convertView.findViewById(R.id.btn_comment);
            holder.likesNum = (TextView) convertView.findViewById(R.id.tvLikes);
            holder.commentNum = (TextView) convertView.findViewById(R.id.tvComments);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.post = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        getCurrentUsername();

        if (getItem(position).getLikes().isEmpty()) {
            holder.likesNum.setVisibility(View.GONE);
        }

//        List<Comment> comments = getItem(position).getComments();
//        holder.commentNum.setText(comments.size());
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading post thread for " + getItem(position).getPost_id());
                ((MainActivity)mContext).onPostThreadSelected(getItem(position), holder.settings);

                //going to need to do something else?
            }
        });

        holder.discussion.setText(getItem(position).getDiscussion());
        String tag = getItem(position).getTags();
        String[] tokens = tag.split(" ");
        String newTag = "";
        int tokenCount = tokens.length;
        for (int i = 0; i < tokenCount; i++) {
            newTag = newTag + "#" + tokens[i] + " ";
        }

        holder.tags.setText(newTag);

        //set time it was posted
        String timestampDiff = getTimestampDifference(getItem(position).getDate_created());
        holder.timeDetails.setText(timestampDiff);

        if (getItem(position).getAnonymity().equals("Anonymous")) {
            holder.username.setText("Anonymous");
        }
        else {
            //get username of poster
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_users_account_settings))
                    .orderByChild(mContext.getString(R.string.field_user_id))
                    .equalTo(getItem(position).getUser_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //currentUsername = ds.getValue(UserAccountSettings.class).getUsername();
                        Log.d(TAG, "onDataChange: found user: " + ds.getValue(UserAccountSettings.class).getUsername());
                        holder.username.setText(ds.getValue(UserAccountSettings.class).getUsername());
                        holder.username.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "onClick: navigating to profile of: " + holder.user.getUsername());
                                Intent intent = new Intent();
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.main_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                                mContext.startActivity(intent);
                            }
                        });
                        holder.settings = ds.getValue(UserAccountSettings.class);
                        holder.comment.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((MainActivity)mContext).onPostThreadSelected(getItem(position), holder.settings);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //get the user object
        Query userQuery = mReference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + ds.getValue(User.class).getUsername());

                    holder.user = ds.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e){
            Log.d(TAG, "onDoubleTap:  double tap detected.");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_posts))
                    .child(mHolder.post.getPost_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        String KeyID = singleSnapshot.getKey();

                        //Case 1: already liked
                        if (mHolder.likeByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mReference.child(mContext.getString(R.string.dbname_posts))
                                    .child(mHolder.post.getPost_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_posts))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.post.getPost_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            //getLikesString();
                        }
                        //Case 2: not liked
                        else if (!mHolder.likeByCurrentUser) {
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new Like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_posts))
                .child(holder.post.getPost_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(holder.post.getPost_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        holder.heart.toggleLike();
        //getLikesString();
        setupLikesButton(holder);
    }

    private void setupLikesButton(final ViewHolder holder) {
        Log.d(TAG, "setupLikesButton: like/unlike by user");

        if (holder.likeByCurrentUser) {
            Log.d(TAG, "setupLikesButton: post is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else {
            Log.d(TAG, "setupLikesButton: post is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: getting current username");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    currentUsername = ds.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getTimestampDifference(String postTimestamp) {
        //Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

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
}
