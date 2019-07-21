package com.example.hashpotatoesv20.Notif;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.hashpotatoesv20.Models.Notification;
import com.example.hashpotatoesv20.R;
import com.example.hashpotatoesv20.Utils.NotificationListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class NotifFragment extends Fragment{

    private static final String TAG = "NotificationFragment";
    private static final int ACTIVITY_NUM = 2;

    public interface onListNotifSelectedListener {
        void onNotifSelected(Notification notification, int activity_num);
    }

    onListNotifSelectedListener mOnListNotifSelectedListener;

    @Override
    public void onAttach(Context context) {
        try {
            mOnListNotifSelectedListener = (onListNotifSelectedListener) getActivity();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
        super.onAttach(context);
    }

    //vars
    private ArrayList<Notification> mNotification;
    private ArrayList<Notification> mPaginatedNotification;
    private ListView mListView;
    private NotificationListAdapter mAdapter;
    private int mResults;

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mNotification = new ArrayList<>();
        mContext = getActivity();

        getNotifications();

        return view;
    }

    private void getNotifications() {
        Log.d(TAG, "getNotification: getting notifications");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_notif))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Notification notification = new Notification();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    notification.setMessage(objectMap.get(getString(R.string.field_message)).toString());
                    notification.setPost_id(objectMap.get(getString(R.string.field_post_id)).toString());
                    notification.setViewer_uid(objectMap.get(getString(R.string.field_viewer_uid)).toString());
                    notification.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    notification.setTag(objectMap.get(getString(R.string.field_tag)).toString());

                    mNotification.add(notification);
                    Log.d(TAG, "onDataChange: testnotif: size: " + mNotification.size());
                }
                displayNotifications();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void displayNotifications(){
        mPaginatedNotification = new ArrayList<>();
        if (mNotification != null) {
            try {
                Collections.sort(mNotification, new Comparator<Notification>() {
                    @Override
                    public int compare(Notification o1, Notification o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iteration = mNotification.size();
                Log.d(TAG, "displayPosts: Checking post size: " + mNotification.size());

                if (iteration > 10){
                    iteration = 10;
                }
                mResults = 10;
                for (int i = 0; i < iteration; i ++){
                    mPaginatedNotification.add(mNotification.get(i));
                }
                mAdapter = new NotificationListAdapter(mContext, R.layout.layout_notif_listview, mPaginatedNotification);
                mListView.setAdapter(mAdapter);
                setListViewHeightBasedOnChildren(mListView);

                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int actPosition = mPaginatedNotification.size() - position - 1;
                        Log.d(TAG, "onItemClick: position:" + actPosition);
                        Log.d(TAG, "onItemClick: " + mPaginatedNotification.get(position));
                        Log.d(TAG, "onItemClick: size: " + position);
                        mOnListNotifSelectedListener.onNotifSelected(mPaginatedNotification.get(position),ACTIVITY_NUM);
                    }
                });
            } catch (NullPointerException e) {
                Log.e(TAG, "displayNotifications: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayNotification: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
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

}
