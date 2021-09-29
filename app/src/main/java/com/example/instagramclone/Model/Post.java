package com.example.instagramclone.Model;

public class Post {
    private String postid;
    private String postimage;
    private String description;
    private String publisher;
    private String strDate ;
    private boolean isdisplay ;

    public Post(String postid, String postimage, String description, String publisher, String strDate, boolean isdisplay) {
        this.postid = postid;
        this.postimage = postimage;
        this.description = description;
        this.publisher = publisher;
        this.strDate = strDate;
        this.isdisplay = isdisplay;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }


    public boolean isIsdisplay() {
        return isdisplay;
    }

    public void setIsdisplay(boolean isdisplay) {
        this.isdisplay = isdisplay;
    }
}