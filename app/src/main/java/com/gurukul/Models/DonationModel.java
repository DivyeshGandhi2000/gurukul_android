package com.gurukul.Models;

public class DonationModel {

    private String id;
    private String userName;
    private String description;
    private String imageUrl;
    private String createdAt;

    public DonationModel(String id, String userName, String description,
                         String imageUrl, String createdAt) {
        this.id          = id;
        this.userName    = userName;
        this.description = description;
        this.imageUrl    = imageUrl;
        this.createdAt   = createdAt;
    }

    public String getId()          { return id; }
    public String getUserName()    { return userName; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }
    public String getCreatedAt()   { return createdAt; }
}