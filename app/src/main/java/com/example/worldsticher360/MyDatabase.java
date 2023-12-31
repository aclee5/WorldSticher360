package com.example.worldsticher360;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MyDatabase {
    private SQLiteDatabase db;
    private Context context;
    private final MyHelper helper;

    public MyDatabase(Context c) {
        context = c;
        helper = new MyHelper(context);
    }

    public long insertPhotoData(String photoPath, String timestamp, String name) {
        db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.NAME, name);
        contentValues.put(Constants.PHOTO_PATH, photoPath);
        contentValues.put(Constants.TIMESTAMP, timestamp);
        long id = db.insert(Constants.TABLE_NAME, null, contentValues);
        return id;
    }

    public Cursor getPhotoData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] columns = {Constants.NAME, Constants.PHOTO_PATH, Constants.TIMESTAMP};
        return db.query(Constants.TABLE_NAME, columns, null, null, null, null, null);

    }
}

