package com.leandro.notifyblock;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotificationHistoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notification_history.db";
    private static final int DATABASE_VERSION = 1;

    public NotificationHistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notifications (_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifications");
        onCreate(db);
    }

    public void addNotification(String content) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO notifications (content) VALUES (?)", new Object[]{content});
    }

    public Cursor getNotificationHistory() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT content FROM notifications", null);
    }

    public void clearAllNotifications() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM notifications");
    }
}