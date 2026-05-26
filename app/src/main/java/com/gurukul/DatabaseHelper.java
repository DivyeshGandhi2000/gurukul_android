package com.gurukul;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StatusManager.db";
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    private static final String TABLE_STATUS = "status";
    private static final String COL_STATUS_ID = "id";
    private static final String COL_IMAGE_PATH = "image_path";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DATE = "date";
    private static final String COL_TYPE = "type";
    private static final String COL_CREATED_DATE = "created_date";
    private static final String COL_IS_VISIBLE = "is_visible";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USERNAME + " TEXT UNIQUE,"
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUserTable);

        String createStatusTable = "CREATE TABLE " + TABLE_STATUS + "("
                + COL_STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_IMAGE_PATH + " TEXT,"
                + COL_NAME + " TEXT,"
                + COL_DESCRIPTION + " TEXT,"
                + COL_DATE + " TEXT,"
                + COL_TYPE + " TEXT,"
                + COL_CREATED_DATE + " TEXT,"
                + COL_IS_VISIBLE + " INTEGER DEFAULT 0)";
        db.execSQL(createStatusTable);

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, "gurukul");
        values.put(COL_PASSWORD, "rbscooty@1971");
        db.insert(TABLE_USERS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        onCreate(db);
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_STATUS +
                    " ADD COLUMN " + COL_IS_VISIBLE + " INTEGER DEFAULT 0");
        }
    }


    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Status operations
    public long insertStatus(String imagePath, String name, String description,
                             String date, String type, boolean isVisible) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IMAGE_PATH, imagePath);
        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DATE, date);
        values.put(COL_TYPE, type);
        values.put(COL_CREATED_DATE, getCurrentDate());
        values.put(COL_IS_VISIBLE, isVisible ? 1 : 0);
        return db.insert(TABLE_STATUS, null, values);
    }

    public List<Status> getAllStatus() {
        List<Status> statusList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STATUS, null, null, null,
                null, null, COL_STATUS_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Status status = new Status();
                status.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS_ID)));
                status.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
                status.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                status.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
                status.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
                status.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
                status.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_DATE)));
                status.setVisible(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_VISIBLE)) == 1
                );

                statusList.add(status);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return statusList;
    }

    public void deleteStatus(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STATUS, COL_STATUS_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteOldStatus() {
        SQLiteDatabase db = this.getWritableDatabase();
        String twoDaysAgo = getTwoDaysAgoDate();
        db.delete(TABLE_STATUS, COL_CREATED_DATE + " <= ?", new String[]{twoDaysAgo});
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getTwoDaysAgoDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        date.setTime(date.getTime() - (2 * 24 * 60 * 60 * 1000));
        return sdf.format(date);
    }

    public Status getStatusById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_STATUS,
                null,
                COL_STATUS_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Status status = null;
        if (cursor != null && cursor.moveToFirst()) {
            status = new Status();
            status.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS_ID)));
            status.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
            status.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
            status.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
            status.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
            status.setType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
            status.setVisible(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_VISIBLE)) == 1);  // Add this line
            cursor.close();
        }

        return status;
    }
    public boolean updateStatus(int id, String imagePath, String name, String description,
                                String date, String type, boolean isVisible) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_IMAGE_PATH, imagePath);
        values.put(COL_NAME, name);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_DATE, date);
        values.put(COL_TYPE, type);
        values.put(COL_IS_VISIBLE, isVisible ? 1 : 0);

        int rowsAffected = db.update(
                TABLE_STATUS,
                values,
                COL_STATUS_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return rowsAffected > 0;
    }

}

