package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.Tag;
import com.example.hashpotatoesv20.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class TagListAdapter extends ArrayAdapter<Tag> {

    private static final String TAG = "TagListAdapter";

    private LayoutInflater mInflater;
    private List<Tag> mTags;
    private int layoutResource;
    private Context mContext;

    public TagListAdapter(@NonNull Context context, @LayoutRes int resource,@NonNull List<Tag> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mTags = objects;
    }

    private static class ViewHolder {
        TextView tag_name, tag_desc;
        ImageView tag_photo_priv, tag_photo_pub;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView == null){
            Log.d(TAG, "getView: inserting info into holder.");
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new TagListAdapter.ViewHolder();

            holder.tag_name = (TextView) convertView.findViewById(R.id.tag_name);
            holder.tag_desc = (TextView) convertView.findViewById(R.id.tag_desc);
            holder.tag_photo_priv = (ImageView) convertView.findViewById(R.id.ivPrivate);
            holder.tag_photo_pub = (ImageView) convertView.findViewById(R.id.ivPublic);

            convertView.setTag(holder);
        }
        else {
            holder = (TagListAdapter.ViewHolder) convertView.getTag();
        }
        Log.d(TAG, "getView: setting testviews and imageviews");
        String tagname = "#" + getItem(position).getTag_name();
        holder.tag_name.setText(tagname);
        holder.tag_desc.setText(getItem(position).getTag_description());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_tags))
                .orderByChild(mContext.getString(R.string.field_tag_id))
                .equalTo(getItem(position).getTag_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    HashMap<String,Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    String privacy = objectMap.get(mContext.getString(R.string.field_privacy)).toString();
                    if (privacy.equals("Private")) {
                        holder.tag_photo_pub.setVisibility(View.INVISIBLE);
                        holder.tag_photo_priv.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.tag_photo_priv.setVisibility(View.INVISIBLE);
                        holder.tag_photo_pub.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
