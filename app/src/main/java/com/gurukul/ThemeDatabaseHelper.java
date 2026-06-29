package com.gurukul.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.gurukul.model.ThemeConfig;
import java.util.ArrayList;
import java.util.List;

public class ThemeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "themes.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "themes";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_KEY = "theme_key";
    private static final String COL_NAME = "display_name";
    private static final String COL_BG_COLOR = "bg_color";
    private static final String COL_BG_DRAWABLE = "bg_drawable";
    private static final String COL_PRIMARY = "primary_color";
    private static final String COL_SECONDARY = "secondary_color";
    private static final String COL_DATEBAR_COLOR = "datebar_color";
    private static final String COL_DATEBAR_DRAWABLE = "datebar_drawable";
    private static final String COL_BORDER = "border_drawable";
    private static final String COL_HEADER = "header_drawable";
    private static final String COL_FOOTER = "footer_drawable";
    private static final String COL_WATERMARK = "watermark_alpha";
    private static final String COL_ICON_BG = "icon_bg";
    private static final String COL_ICON_TEXT = "icon_text";
    private static final String COL_ICON_TEXT_COLOR = "icon_text_color";
    private static final String COL_SORT = "sort_order";
    private static final String COL_BUILTIN = "is_built_in";

    private static ThemeDatabaseHelper instance;

    public static synchronized ThemeDatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new ThemeDatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private ThemeDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_KEY + " TEXT UNIQUE NOT NULL, " +
                COL_NAME + " TEXT, " +
                COL_BG_COLOR + " TEXT, " +
                COL_BG_DRAWABLE + " TEXT, " +
                COL_PRIMARY + " TEXT, " +
                COL_SECONDARY + " TEXT, " +
                COL_DATEBAR_COLOR + " TEXT, " +
                COL_DATEBAR_DRAWABLE + " TEXT, " +
                COL_BORDER + " TEXT, " +
                COL_HEADER + " TEXT, " +
                COL_FOOTER + " TEXT, " +
                COL_WATERMARK + " REAL DEFAULT 0.2, " +
                COL_ICON_BG + " TEXT, " +
                COL_ICON_TEXT + " TEXT, " +
                COL_ICON_TEXT_COLOR + " TEXT, " +
                COL_SORT + " INTEGER DEFAULT 99, " +
                COL_BUILTIN + " INTEGER DEFAULT 0)");

        seedBuiltInThemes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // ─── Seed all your existing themes once ──────────────────────────────────
    private void seedBuiltInThemes(SQLiteDatabase db) {
        // classic — uses item_status_xml layout
        insert(db, new ThemeConfig(
                "classic", "Classic Theme",
                "#8B0000", null,
                "#8B0000", "#000000",
                "#8B0000", null,
                "rounded_border", "card_classic", "card_classic",
                0.2f,
                "#EEEEEE", "Cl", "#424242",
                1, true));

        // modern — uses item_status_xml layout, overrides colors
        insert(db, new ThemeConfig(
                "modern", "Modern Theme",
                "#0D47A1", null,
                "#0D47A1", "#2E7D32",
                "#1565C0", null,
                "rounded_border", "card_modern", "card_modern",
                0.2f,
                "#F3E5F5", "Mo", "#7B1FA2",
                2, true));

        insert(db, new ThemeConfig(
                "blue", "Cyan Blue Theme",
                null, "back",
                "#0891b2", "#000000",
                "#0891b2", null,
                "rounded_border", null, null,
                0f,
                "#E0F7FA", "Cy", "#00838F",
                4, true));

        insert(db, new ThemeConfig(
                "green", "Green Theme",
                null, "green_bg_new",
                "#2E7D32", "#5D4037",
                null, "btn_primary_bg",
                "rounded_green_border", null, null,
                0f,
                "#E8F5E9", "Gr", "#2E7D32",
                5, true));

        // orange — uses item_status_orange layout
        insert(db, new ThemeConfig(
                "orange", "Orange Theme",
                null, "orange_bg",
                "#E65100", "#BF360C",
                null, "card_orange_bg",
                "rounded_orange_border", null, null,
                0f,
                "#FFF3E0", "Or", "#E65100",
                6, true));

        // yellow — uses item_status_yellow layout
        insert(db, new ThemeConfig(
                "yellow", "Yellow Theme",
                null, "yellow_bg",
                "#FBC02D", "#E65100",
                null, "card_yellow_bg",
                "rounded_yellow_border", null, null,
                0f,
                "#FFFDE7", "Ye", "#FBC02D",
                7, true));
    }

    private void insert(SQLiteDatabase db, ThemeConfig t) {
        db.insert(TABLE, null, toValues(t));
    }

    // ─── Public CRUD ─────────────────────────────────────────────────────────
    public List<ThemeConfig> getAllThemes() {
        List<ThemeConfig> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, COL_SORT + " ASC");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }

    public ThemeConfig getTheme(String themeKey) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, COL_KEY + "=?", new String[]{themeKey}, null, null, null);
        ThemeConfig t = null;
        if (c.moveToFirst()) t = fromCursor(c);
        c.close();
        return t;
    }

    public long addTheme(ThemeConfig t) {
        return getWritableDatabase().insert(TABLE, null, toValues(t));
    }

    public int updateTheme(ThemeConfig t) {
        return getWritableDatabase().update(TABLE, toValues(t), COL_KEY + "=?",
                new String[]{t.getThemeKey()});
    }

    public int deleteTheme(String themeKey) {
        return getWritableDatabase().delete(TABLE, COL_KEY + "=? AND " + COL_BUILTIN + "=0",
                new String[]{themeKey});
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private ContentValues toValues(ThemeConfig t) {
        ContentValues v = new ContentValues();
        v.put(COL_KEY, t.getThemeKey());
        v.put(COL_NAME, t.getDisplayName());
        v.put(COL_BG_COLOR, t.getBgColor());
        v.put(COL_BG_DRAWABLE, t.getBgDrawable());
        v.put(COL_PRIMARY, t.getPrimaryColor());
        v.put(COL_SECONDARY, t.getSecondaryColor());
        v.put(COL_DATEBAR_COLOR, t.getDateBarColor());
        v.put(COL_DATEBAR_DRAWABLE, t.getDateBarDrawable());
        v.put(COL_BORDER, t.getBorderDrawable());
        v.put(COL_HEADER, t.getHeaderDrawable());
        v.put(COL_FOOTER, t.getFooterDrawable());
        v.put(COL_WATERMARK, t.getWatermarkAlpha());
        v.put(COL_ICON_BG, t.getIconBg());
        v.put(COL_ICON_TEXT, t.getIconText());
        v.put(COL_ICON_TEXT_COLOR, t.getIconTextColor());
        v.put(COL_SORT, t.getSortOrder());
        v.put(COL_BUILTIN, t.isBuiltIn() ? 1 : 0);
        return v;
    }

    private ThemeConfig fromCursor(Cursor c) {
        ThemeConfig t = new ThemeConfig();
        t.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        t.setThemeKey(c.getString(c.getColumnIndexOrThrow(COL_KEY)));
        t.setDisplayName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        t.setBgColor(c.getString(c.getColumnIndexOrThrow(COL_BG_COLOR)));
        t.setBgDrawable(c.getString(c.getColumnIndexOrThrow(COL_BG_DRAWABLE)));
        t.setPrimaryColor(c.getString(c.getColumnIndexOrThrow(COL_PRIMARY)));
        t.setSecondaryColor(c.getString(c.getColumnIndexOrThrow(COL_SECONDARY)));
        t.setDateBarColor(c.getString(c.getColumnIndexOrThrow(COL_DATEBAR_COLOR)));
        t.setDateBarDrawable(c.getString(c.getColumnIndexOrThrow(COL_DATEBAR_DRAWABLE)));
        t.setBorderDrawable(c.getString(c.getColumnIndexOrThrow(COL_BORDER)));
        t.setHeaderDrawable(c.getString(c.getColumnIndexOrThrow(COL_HEADER)));
        t.setFooterDrawable(c.getString(c.getColumnIndexOrThrow(COL_FOOTER)));
        t.setWatermarkAlpha(c.getFloat(c.getColumnIndexOrThrow(COL_WATERMARK)));
        t.setIconBg(c.getString(c.getColumnIndexOrThrow(COL_ICON_BG)));
        t.setIconText(c.getString(c.getColumnIndexOrThrow(COL_ICON_TEXT)));
        t.setIconTextColor(c.getString(c.getColumnIndexOrThrow(COL_ICON_TEXT_COLOR)));
        t.setSortOrder(c.getInt(c.getColumnIndexOrThrow(COL_SORT)));
        t.setBuiltIn(c.getInt(c.getColumnIndexOrThrow(COL_BUILTIN)) == 1);
        return t;
    }
}