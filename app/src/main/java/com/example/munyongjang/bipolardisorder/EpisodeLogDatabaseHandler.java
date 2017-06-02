package com.example.munyongjang.bipolardisorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by munyongjang on 3/28/17.
 */
// data base for storing
public class EpisodeLogDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EpisodeLogDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EPISODES = "episodes";

    // the columns needed for the tables
    private static final String KEY_ID = "id";
    private static final String KEY_DATE_START = "start_date";
    private static final String KEY_DATE_END = "end_date";
    private static final String KEY_TYPE = "type";

    public EpisodeLogDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public EpisodeLogDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("ELDH", "constructor called");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("ELDH", "onCreate called");
        String CREATE_EPISODES_TABLE = "CREATE TABLE " + TABLE_EPISODES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE_START + " INTEGER,"
                + KEY_DATE_END + " INTEGER,"+ KEY_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_EPISODES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EPISODES);
        onCreate(db);
    }

    // inserting
    public void insertEpisode(long start, long end, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE_START, start);
        values.put(KEY_DATE_END, end);
        values.put(KEY_TYPE, type);

        db.insert(TABLE_EPISODES, null, values);
        db.close();
    }

    // returns all the episodes
    public List<List<Object>> getAllEpisodes() {
        List<List<Object>> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EPISODES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                long startDate = cursor.getLong(1);
                long endDate = cursor.getLong(2);
                String type = cursor.getString(3);
                ArrayList<Object> iList = new ArrayList<>();
                iList.add(startDate);
                iList.add(endDate);
                iList.add(type);
                iList.add(id);
                list.add(iList);
            } while(cursor.moveToNext());
        }
        return list;
    }

    // deleting by the "primary key"
    public boolean deleteEntry(int key) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_EPISODES, KEY_ID + "=" + key, null) > 0;
    }
}
