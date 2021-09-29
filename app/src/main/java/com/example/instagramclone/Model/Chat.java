package com.example.instagramclone.Model;

public class Chat {

    String receiver ;
    String sender ;
    String message ;
    String strDate ;
    boolean isseen ;
    String photoUrl ;
    int feeling = -1;
    String link ;
    String messageid ;

    public Chat(String receiver, String sender, String message, String strDate, boolean isseen, String photoUrl, int feeling, String link, String messageid) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        this.strDate = strDate;
        this.isseen = isseen;
        this.photoUrl = photoUrl;
        this.feeling = feeling;
        this.link = link;
        this.messageid = messageid;
    }

    public Chat() {
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }
}
