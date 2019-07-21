package com.example.hashpotatoesv20.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hashpotatoesv20.Models.Notification;
import com.example.hashpotatoesv20.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class NotificationListAdapter extends ArrayAdapter<Notification> {

    private static final String TAG = "NotificationListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private int usernameLen;
    private Context mContext;

    public NotificationListAdapter(@NonNull Context context, int resource, @NonNull List<Notification> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
    }

    private static class ViewHolder {
        TextView Message, Timestamp, Accept, Decline;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            Log.d(TAG, "getView: inserting info into holder.");
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new NotificationListAdapter.ViewHolder();

            holder.Message = (TextView) convertView.findViewById(R.id.notif_message);
            holder.Timestamp = (TextView) convertView.findViewById(R.id.notif_timestamp);

            convertView.setTag(holder);
        }
        else {
            holder = (NotificationListAdapter.ViewHolder) convertView.getTag();
        }

        final SpannableStringBuilder sb = new SpannableStringBuilder(getItem(position).getMessage());
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        StringTokenizer str = new StringTokenizer (getItem(position).getMessage());
        usernameLen = str.nextToken().length();
        sb.setSpan(bss, 0, usernameLen, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        holder.Message.setText(sb);
        String timestampDifference = getTimestampDifference(getItem(position).getDate_created());
//        if (!timestampDifference.equals("0")) {
            holder.Timestamp.setText(timestampDifference);
//        } else {
//            holder.Timestamp.setText("Today");
//        }
        
        return convertView;
    }


    /**
     * Returns a string representing the number of days aho the post was made
     *
     * @return
     */
    private String getTimestampDifference(String postTimestamp) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

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
            } else if (tsDiff < 60) { //less than one hour
                if (tsDiff == 1) {
                    time = tsDiff + " MIN AGO";
                } else {
                    time = tsDiff + " MINS AGO";
                }
                return time;
            } else if (tsDiff < 1440) { //less than one day
                tsDiff = tsDiff / 60;
                if (tsDiff == 1) {
                    time = tsDiff + " HOUR AGO";
                } else {
                    time = tsDiff + " HOURS AGO";
                }
                return time;
            } else {
                tsDiff = tsDiff / 60 / 24;
                if (tsDiff == 1) {
                    time = tsDiff + " DAY AGO";
                } else {
                    time = tsDiff + " DAYS AGO";
                }
                return time;
            }
        } catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }


}
