package com.example.hashpotatoesv20.Models;

public class UserAccountSettings {

    private String description;
    private String display_name;
    private String major;
    private long posts;
    private String profile_photo;
    private String username;
    private String website;
    private String year;
    private long hashtags;

    public UserAccountSettings(String description, String display_name, String major,
                               long posts, String profile_photo, String username,
                               String website, String year, long hashtags) {
        this.description = description;
        this.display_name = display_name;
        this.major = major;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.username = username;
        this.website = website;
        this.year = year;
        this.hashtags = hashtags;
    }

    public UserAccountSettings() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public long getHashtags() {
        return hashtags;
    }

    public void setHashtags(long hashtags) {
        this.hashtags = hashtags;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", major='" + major + '\'' +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", website='" + website + '\'' +
                ", year='" + year + '\'' +
                ", hashtags=" + hashtags +
                '}';
    }
}
