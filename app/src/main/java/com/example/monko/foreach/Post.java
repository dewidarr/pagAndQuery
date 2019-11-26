package com.example.monko.foreach;

public class Post {

    private String desc;
    private String image;
    private String username;
    private String userImage;
    private long counter;
    private String Date;
    private String key;

    public Post(){

    }

    public Post(String desc, String image,long counter,String userImage) {
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.userImage = userImage;
        this.counter = counter;

    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
