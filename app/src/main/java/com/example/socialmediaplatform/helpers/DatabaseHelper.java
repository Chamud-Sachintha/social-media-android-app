package com.example.socialmediaplatform.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserDB";
    private static final String TABLE_NAME = "users";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "NAME";
    private static final String COL_3 = "EMAIL";
    private static final String COL_4 = "PASSWORD";

//    chat store messages table

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SENDER_ID + " TEXT, "
                + COLUMN_RECEIVER_ID + " TEXT, "
                + COLUMN_MESSAGE + " TEXT, "
                + COLUMN_TIMESTAMP + " INTEGER)";
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, EMAIL TEXT, PASSWORD TEXT)");
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, email);
        contentValues.put(COL_4, password);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Returns true if inserted successfully
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE EMAIL = ? AND PASSWORD = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public void addMessage(String senderId, String receiverId, String message, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER_ID, senderId);
        values.put(COLUMN_RECEIVER_ID, receiverId);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, timestamp);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public Cursor getMessages(String senderId, String receiverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MESSAGES, null,
                "(" + COLUMN_SENDER_ID + "=? AND " + COLUMN_RECEIVER_ID + "=?) OR ("
                        + COLUMN_SENDER_ID + "=? AND " + COLUMN_RECEIVER_ID + "=?)",
                new String[]{senderId, receiverId, receiverId, senderId},
                null, null, COLUMN_TIMESTAMP + " ASC");
    }

    @SuppressLint("Range")
    public String getUserIdFromEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userId = null;

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID}, // Columns to retrieve
                COL_3 + "=?", // WHERE clause
                new String[]{email}, // Arguments for the WHERE clause
                null, null, null); // Group by, having, order by

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userId = cursor.getString(cursor.getColumnIndex("ID"));
            }
            cursor.close();
        }

        return userId; // Return the user ID or null if not found
    }

    public SQLiteDatabase getReadableDatabaseInstance() {
        return this.getReadableDatabase();
    }

}
