package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.Comment;
import com.example.hashpotatoesv20.Models.Post;
import com.example.hashpotatoesv20.Models.UserAccountSettings;
import com.example.hashpotatoesv20.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileListAdapter extends ArrayAdapter<Post> {

    private static final String TAG = "PostListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    OnAdapterItemClickListener mOnAdapterItemClickListener;

    public ProfileListAdapter(@NonNull Context context, int resource, @NonNull List<Post> objects, OnAdapterItemClickListener onAdapterItemClickListener) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
        mOnAdapterItemClickListener = onAdapterItemClickListener;
    }

    private static class ViewHolder{
        TextView Discussion, Tag, Username, timestamp;
        ImageView like, edit;
    }

    public interface OnAdapterItemClickListener {
        public void onClickImage(Post post, int activity_number);
        public void onViewClicked(Post post, int activity_number);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.Discussion = (TextView) convertView.findViewById(R.id.edit_post_discussion);
            holder.Tag = (TextView) convertView.findViewById(R.id.edit_post_tag) ;
            holder.Username = (TextView) convertView.findViewById(R.id.edit_post_username);
            holder.timestamp = (TextView) convertView.findViewById(R.id.edit_post_timestamp);
            holder.like = (ImageView) convertView.findViewById(R.id.btn_heart_white);
            holder.edit = (ImageView) convertView.findViewById(R.id.btn_edit) ;

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //set Discussion
        holder.Discussion.setText(getItem(position).getDiscussion());
        //set time diff
        String timestampDifference = getTimestampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.timestamp.setText(timestampDifference + "d");
        }else{
            holder.timestamp.setText("Today");
        }
        //set the username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.Username.setText(
                    singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:Test");
                mOnAdapterItemClickListener.onClickImage(getItem(position),position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnAdapterItemClickListener.onViewClicked(getItem(position), position);
            }
        });

        return convertView;
    }


    /**
     * Returns a string representing the number of days aho the post was made
     * @return
     */
    private String getTimestampDifference(Post post) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String postTimestamp = post.getDate_created();
        try {
            timestamp = sdf.parse(postTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        }
        catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }


}
