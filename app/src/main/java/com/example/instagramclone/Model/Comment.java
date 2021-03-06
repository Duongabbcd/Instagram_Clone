package com.example.instagramclone.Model;

public class Comment {
    private String comment;
    private String publisher;
    private String commentid;
    private String strDate ;

    public Comment(String comment, String publisher, String commentid, String strDate) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
        this.strDate = strDate;
    }

    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }
}