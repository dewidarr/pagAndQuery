package com.dewidar.foreach.foreach;

/**
 * Created by amran on 4/24/2017.
 */

public class Comment {

    private String comment;
    private String userName;
    private String userImage;

    public Comment(){

    }

    public Comment(String comment,String userImage) {
        this.comment = comment;
        this.userName=userName;
        this.userImage=userImage;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}