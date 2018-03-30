package org.example.eventos.modelo.instagram;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Created by jordi 
 */

/*

https://www.instagram.com/developer/endpoints/users/

GET/users/self
RESPONSE
https://api.instagram.com/v1/users/self/?access_token=ACCESS-TOKEN
{
  "data": {
    "id": "1574083",
    "username": "snoopdogg",
    "full_name": "Snoop Dogg",
    "profile_picture": "http://distillery.s3.amazonaws.com/profiles/profile_1574083_75sq_1295469061.jpg",
    "bio": "This is my bio",
    "website": "http://snoopdogg.com",
    "is_business": false,
    "counts": {
      "media": 1320,
      "follows": 420,
      "followed_by": 3410
    }
  }
}

http://www.jsonschema2pojo.org
*/


public class RespuestaGetUsuarioInstagram {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

public class Counts {

    @SerializedName("media")
    @Expose
    private Integer media;
    @SerializedName("follows")
    @Expose
    private Integer follows;
    @SerializedName("followed_by")
    @Expose
    private Integer followedBy;

    public Integer getMedia() {
        return media;
    }

    public void setMedia(Integer media) {
        this.media = media;
    }

    public Integer getFollows() {
        return follows;
    }

    public void setFollows(Integer follows) {
        this.follows = follows;
    }

    public Integer getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(Integer followedBy) {
        this.followedBy = followedBy;
    }



}

public class Data {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("profile_picture")
    @Expose
    private String profilePicture;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("website")
    @Expose
    private String website;
    @SerializedName("is_business")
    @Expose
    private Boolean isBusiness;
    @SerializedName("counts")
    @Expose
    private Counts counts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Boolean getIsBusiness() {
        return isBusiness;
    }

    public void setIsBusiness(Boolean isBusiness) {
        this.isBusiness = isBusiness;
    }

    public Counts getCounts() {
        return counts;
    }

    public void setCounts(Counts counts) {
        this.counts = counts;
    }



}



}


