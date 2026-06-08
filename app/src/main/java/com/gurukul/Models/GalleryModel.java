package com.gurukul.Models;

public class GalleryModel {

    private String id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String createdAt;

    public GalleryModel(String id, String title, String description,
                        String category, String imageUrl, String createdAt) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.imageUrl    = imageUrl;
        this.createdAt   = createdAt;
    }

    public String getId()          { return id; }
    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public String getCategory()    { return category; }
    public String getImageUrl()    { return imageUrl; }
    public String getCreatedAt()   { return createdAt; }
}