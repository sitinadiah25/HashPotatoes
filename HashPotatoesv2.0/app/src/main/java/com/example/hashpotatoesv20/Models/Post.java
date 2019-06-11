package com.example.hashpotatoesv20.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {

    String discussion;
    String date_created;
    String user_id;
    String post_id;
    String tags;
    String anonymity;
    String comments;
    long likes;

    public Post(String discussion, String date_created, String user_id,
                String post_id, String tags, String anonymity, String comments,
                long likes) {
        this.discussion = discussion;
        this.date_created = date_created;
        this.user_id = user_id;
        this.post_id = post_id;
        this.tags = tags;
        this.anonymity = anonymity;
        this.comments = comments;
        this.likes = likes;
    }

    public Post() {

    }

    protected Post(Parcel in) {
        discussion = in.readString();
        date_created = in.readString();
        user_id = in.readString();
        post_id = in.readString();
        tags = in.readString();
        anonymity = in.readString();
        comments = in.readString();
        likes = in.readLong();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getDiscussion() {
        return discussion;
    }

    public void setDiscussion(String discussion) {
        this.discussion = discussion;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAnonymity() {
        return anonymity;
    }

    public void setAnonymity(String anonymity) {
        this.anonymity = anonymity;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Post{" +
                "discussion='" + discussion + '\'' +
                ", date_created='" + date_created + '\'' +
                ", user_id='" + user_id + '\'' +
                ", post_id='" + post_id + '\'' +
                ", tags='" + tags + '\'' +
                ", anonymity='" + anonymity + '\'' +
                ", comments='" + comments + '\'' +
                ", likes=" + likes +
                '}';
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(discussion);
        dest.writeString(date_created);
        dest.writeString(user_id);
        dest.writeString(post_id);
        dest.writeString(tags);
        dest.writeString(anonymity);
        dest.writeString(comments);
        dest.writeLong(likes);
    }
}
