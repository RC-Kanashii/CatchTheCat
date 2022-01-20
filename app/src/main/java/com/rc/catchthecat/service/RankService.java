package com.rc.catchthecat.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class RankService extends SQLiteOpenHelper {
    public RankService(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table rank(_id integer primary key autoincrement," +
                " date timestamp default current_timestamp, steps integer, difficulty varchar)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void add(SQLiteDatabase database, int steps, String difficulty) {
        ContentValues values = new ContentValues();
        values.put("steps", steps);
        values.put("difficulty", difficulty);
        database.insert("rank", null, values);
    }

    public Cursor queryTop10(SQLiteDatabase database) {
        return database.query("rank", new String[]{"_id", "date", "steps", "difficulty"}, null, null, null, null, "steps asc", "10");
    }
}
