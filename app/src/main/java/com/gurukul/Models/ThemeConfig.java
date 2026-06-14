package com.gurukul.model;

public class ThemeConfig {
    private int id;
    private String themeKey;        // "classic", "blue", "custom1"
    private String displayName;     // "Classic Theme"
    private String bgColor;         // "#8B0000" or null if using drawable
    private String bgDrawable;      // "classic_bg" or null
    private String primaryColor;    // Main brand color
    private String secondaryColor;  // Secondary text color
    private String dateBarColor;    // Date bar background color
    private String dateBarDrawable; // or drawable name
    private String borderDrawable;  // "rounded_border", "rounded_green_border"
    private String headerDrawable;  // "card_classic", "card_modern" or null
    private String footerDrawable;  // same as header usually
    private float watermarkAlpha;   // 0f to hide, 0.2f to show
    private String iconBg;          // Circle icon bg color in theme list "#E8F5E9"
    private String iconText;        // "Gr", "Bl" etc.
    private String iconTextColor;   // "#2E7D32"
    private int sortOrder;          // display order
    private boolean isBuiltIn;      // can't be deleted

    public ThemeConfig() {}

    // Full constructor
    public ThemeConfig(String themeKey, String displayName,
                       String bgColor, String bgDrawable,
                       String primaryColor, String secondaryColor,
                       String dateBarColor, String dateBarDrawable,
                       String borderDrawable, String headerDrawable, String footerDrawable,
                       float watermarkAlpha,
                       String iconBg, String iconText, String iconTextColor,
                       int sortOrder, boolean isBuiltIn) {
        this.themeKey = themeKey;
        this.displayName = displayName;
        this.bgColor = bgColor;
        this.bgDrawable = bgDrawable;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.dateBarColor = dateBarColor;
        this.dateBarDrawable = dateBarDrawable;
        this.borderDrawable = borderDrawable;
        this.headerDrawable = headerDrawable;
        this.footerDrawable = footerDrawable;
        this.watermarkAlpha = watermarkAlpha;
        this.iconBg = iconBg;
        this.iconText = iconText;
        this.iconTextColor = iconTextColor;
        this.sortOrder = sortOrder;
        this.isBuiltIn = isBuiltIn;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getThemeKey() { return themeKey; }
    public void setThemeKey(String themeKey) { this.themeKey = themeKey; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public String getBgDrawable() { return bgDrawable; }
    public void setBgDrawable(String bgDrawable) { this.bgDrawable = bgDrawable; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public String getDateBarColor() { return dateBarColor; }
    public void setDateBarColor(String dateBarColor) { this.dateBarColor = dateBarColor; }
    public String getDateBarDrawable() { return dateBarDrawable; }
    public void setDateBarDrawable(String dateBarDrawable) { this.dateBarDrawable = dateBarDrawable; }
    public String getBorderDrawable() { return borderDrawable; }
    public void setBorderDrawable(String borderDrawable) { this.borderDrawable = borderDrawable; }
    public String getHeaderDrawable() { return headerDrawable; }
    public void setHeaderDrawable(String headerDrawable) { this.headerDrawable = headerDrawable; }
    public String getFooterDrawable() { return footerDrawable; }
    public void setFooterDrawable(String footerDrawable) { this.footerDrawable = footerDrawable; }
    public float getWatermarkAlpha() { return watermarkAlpha; }
    public void setWatermarkAlpha(float watermarkAlpha) { this.watermarkAlpha = watermarkAlpha; }
    public String getIconBg() { return iconBg; }
    public void setIconBg(String iconBg) { this.iconBg = iconBg; }
    public String getIconText() { return iconText; }
    public void setIconText(String iconText) { this.iconText = iconText; }
    public String getIconTextColor() { return iconTextColor; }
    public void setIconTextColor(String iconTextColor) { this.iconTextColor = iconTextColor; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isBuiltIn() { return isBuiltIn; }
    public void setBuiltIn(boolean builtIn) { isBuiltIn = builtIn; }
}