package com.example.hashpotatoesv20.Models;

public class Post {

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
}
