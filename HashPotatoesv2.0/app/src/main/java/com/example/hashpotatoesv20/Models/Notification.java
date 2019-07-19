package com.example.hashpotatoesv20.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String message;
    private String post_id;
    private String tag;
    private String viewer_uid;
    private String date_created;

    public Notification(){    }

    public Notification(String message, String post_id, String tag, String viewer_uid, String date_created) {
        this.message = message;
        this.post_id = post_id;
        this.tag = tag;
        this.viewer_uid = viewer_uid;
        this.date_created = date_created;
    }

    protected Notification(Parcel in) {
        message = in.readString();
        post_id = in.readString();
        tag = in.readString();
        viewer_uid = in.readString();
        date_created = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(post_id);
        dest.writeString(tag);
        dest.writeString(viewer_uid);
        dest.writeString(date_created);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getViewer_uid() {
        return viewer_uid;
    }

    public void setViewer_uid(String viewer_uid) {
        this.viewer_uid = viewer_uid;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "message='" + message + '\'' +
                ", post_id='" + post_id + '\'' +
                ", tag='" + tag + '\'' +
                ", viewer_uid='" + viewer_uid + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
