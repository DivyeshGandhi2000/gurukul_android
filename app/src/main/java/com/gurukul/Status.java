package com.gurukul;

public class Status {
    private int id;
    private String imagePath;
    private String name;
    private String description;
    private String date;
    private String type;
    private String createdDate;
    private boolean isVisible = false;
    public Status() {}

    public Status(String imagePath, String name, String description, String date, String type) {
        this.imagePath = imagePath;
        this.name = name;
        this.description = description;
        this.date = date;
        this.type = type;
        this.isVisible = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getStatusText() {
        String emoji = getEmojiForType();
        return emoji + " " + name + " - " + description + "\n📅 " + date;
    }
    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    private String getEmojiForType() {
        switch (type.toLowerCase()) {
            case "birthday": return "🎂🎉";
            case "anniversary": return "💑❤️";
            case "punyatithi": return "🙏🕉️";
            default: return "✨";
        }
    }
}


