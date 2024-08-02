package com.leandro.notifyblock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeywordsSettingsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "keywords_settings.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_KEYWORDS = "keywords";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_KEYWORD = "keyword";
    private static final String COLUMN_PACKAGE_NAME = "package_name";

    public KeywordsSettingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_KEYWORDS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_KEYWORD + " TEXT, " +
                COLUMN_PACKAGE_NAME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYWORDS);
        onCreate(db);
    }

    public void addOrUpdateKeyword(String keyword, String packageName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEYWORD, keyword);
        values.put(COLUMN_PACKAGE_NAME, packageName);

        int rowsUpdated = db.update(
                TABLE_KEYWORDS,
                values,
                COLUMN_PACKAGE_NAME + " = ?",
                new String[]{packageName}
        );
        if (rowsUpdated == 0) {
            db.insert(TABLE_KEYWORDS, null, values);
        }
        db.close();
    }

    public Cursor getKeywordsByPackage(String packageName) {
        SQLiteDatabase db = getReadableDatabase();
        String[] selectionArgs = {packageName};
        return db.rawQuery("SELECT keyword FROM " + TABLE_KEYWORDS + " WHERE " + COLUMN_PACKAGE_NAME + " = ?", selectionArgs);
    }

    public Cursor getAllKeywords() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_KEYWORDS, new String[]{COLUMN_KEYWORD, COLUMN_PACKAGE_NAME},
                null, null, null, null, null);
    }

    public void clearKeywordsByPackage(String packageName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_KEYWORDS, COLUMN_PACKAGE_NAME + "=?", new String[]{packageName});
    }

    public void clearAllKeywords() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_KEYWORDS);
    }
}