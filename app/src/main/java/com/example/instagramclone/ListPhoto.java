package com.example.instagramclone;

import java.util.List;

public class ListPhoto {
    private int type ;
    private List<Photo> photos ;

    public ListPhoto(int type, List<Photo> photos) {
        this.type = type;
        this.photos = photos;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
