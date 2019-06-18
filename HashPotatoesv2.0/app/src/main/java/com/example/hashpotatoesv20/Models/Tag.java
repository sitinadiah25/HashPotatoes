package com.example.hashpotatoesv20.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Tag implements Parcelable {
    String privacy;
    String tag_id;
    String tag_name;
    String tag_description;
    String owner_id;
    List<String> post_ids;
    List<String> whitelist;

    public Tag(String privacy, String tag_id, String tag_name, String tag_description, String owner_id, List<String> post_ids, List<String> whitelist) {
        this.privacy = privacy;
        this.tag_id = tag_id;
        this.tag_name = tag_name;
        this.tag_description = tag_description;
        this.owner_id = owner_id;
        this.post_ids = post_ids;
        this.whitelist = whitelist;
    }

    public Tag() {

    }

    protected Tag(Parcel in) {
        privacy = in.readString();
        tag_id = in.readString();
        tag_name = in.readString();
        tag_description = in.readString();
        owner_id = in.readString();
        post_ids = in.createStringArrayList();
        whitelist = in.createStringArrayList();
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTag_description() {
        return tag_description;
    }

    public void setTag_description(String tag_description) {
        this.tag_description = tag_description;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public List<String> getPost_ids() {
        return post_ids;
    }

    public void setPost_ids(List<String> post_ids) {
        this.post_ids = post_ids;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "privacy='" + privacy + '\'' +
                ", tag_id='" + tag_id + '\'' +
                ", tag_name='" + tag_name + '\'' +
                ", tag_description='" + tag_description + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", post_ids=" + post_ids +
                ", whitelist=" + whitelist +
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
        dest.writeString(privacy);
        dest.writeString(tag_id);
        dest.writeString(tag_name);
        dest.writeString(tag_description);
        dest.writeString(owner_id);
        dest.writeStringList(post_ids);
        dest.writeStringList(whitelist);
    }
}


