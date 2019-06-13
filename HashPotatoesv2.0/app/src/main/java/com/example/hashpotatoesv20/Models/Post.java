package com.example.hashpotatoesv20.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Post implements Parcelable {

    private String discussion;
    private String date_created;
    private String user_id;
    private String post_id;
    private String tags;
    private String anonymity;
    private List<Like> likes;
    private List<Comment> comments;

    public Post(){

    }

    public Post(String discussion, String date_created, String user_id, String post_id, String tags, String anonymity, List<Like> likes, List<Comment> comments) {
        this.discussion = discussion;
        this.date_created = date_created;
        this.user_id = user_id;
        this.post_id = post_id;
        this.tags = tags;
        this.anonymity = anonymity;
        this.likes = likes;
        this.comments = comments;
    }

    protected Post(Parcel in) {
        discussion = in.readString();
        date_created = in.readString();
        user_id = in.readString();
        post_id = in.readString();
        tags = in.readString();
        anonymity = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(discussion);
        dest.writeString(date_created);
        dest.writeString(user_id);
        dest.writeString(post_id);
        dest.writeString(tags);
        dest.writeString(anonymity);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}


