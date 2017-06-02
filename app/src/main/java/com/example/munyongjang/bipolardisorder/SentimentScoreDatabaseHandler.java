package com.example.munyongjang.bipolardisorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.api.services.language.v1.model.Sentiment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by munyongjang on 3/22/17.
 */

// separate database for sentiments
public class SentimentScoreDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ApplicationSentimentDataBase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SENTIMENTS = "sentiments";

    // the columns needed for the tables
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_SCORE = "score";
    private static final String KEY_MAGNITUDE = "magnitude";
    private static final String KEY_SENDER = "sender";

    // just to save calculation
    Map<Long, Integer> cntMap;

    public SentimentScoreDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SentimentScoreDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("SSDH", "Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("SSDH", "onCreate");
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SENTIMENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                + KEY_SCORE + " INTEGER," + KEY_MAGNITUDE + " REAL," + KEY_SENDER + " TEXT" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENTIMENTS);
        onCreate(db);
    }

    // adding a sentiment to the sqlite
    public void addSentimentScore(SentimentScore sentimentScore) {
        Log.d("add_Sentiment", sentimentScore.getSender());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, sentimentScore.getTime());
        values.put(KEY_MAGNITUDE, sentimentScore.getMagnitude());
        values.put(KEY_SCORE, sentimentScore.getScore());
        values.put(KEY_SENDER, sentimentScore.getSender());

        db.insert(TABLE_SENTIMENTS, null, values);
        db.close();
    }

    // get it by time and sender
    public boolean containsSentimentInformation(long time, String sender) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "select count(*) from " + TABLE_SENTIMENTS + " where " + KEY_TIME + " = " +
                time + " AND " + KEY_SENDER + " = " + sender;

        SQLiteStatement statement = db.compileStatement(sql);
        return statement.simpleQueryForLong() > 0;
    }

    public List<SentimentScore> getAllSentimentScores() {
        List<SentimentScore> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SENTIMENTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(1);
                double magnitude = cursor.getDouble(2);
                double score = cursor.getDouble(3);
                String sender = cursor.getString(4);
                SentimentScore sentimentScore = new SentimentScore(time, score, magnitude, sender);
                list.add(sentimentScore);
            } while(cursor.moveToNext());
        }
        return list;
    }

    // TODO: this may not work if I don't send a text message on some days and receive text messages on those days
    public Map<Long, Double> getAveragePerDay() {
        Map<Long, Double> cumulMap = new HashMap<>();
        cntMap = new HashMap<>();
        List<SentimentScore> sentimentScoreList = getAllSentimentScores();

        for (SentimentScore sc : sentimentScoreList) {
            Calendar scCal = Calendar.getInstance();
            scCal.setTimeInMillis(sc.getTime());

            scCal.set(Calendar.HOUR_OF_DAY, 0);
            scCal.set(Calendar.MINUTE, 0);
            scCal.set(Calendar.SECOND, 0);
            scCal.set(Calendar.MILLISECOND, 0);

            long scTime = scCal.getTimeInMillis();
            if (!cumulMap.containsKey(scTime)) {
                cumulMap.put(scTime, sc.getScore());
                cntMap.put(scTime, 1);
            } else {
                cumulMap.put(scTime, cumulMap.get(scTime) + sc.getScore());
                cntMap.put(scTime, cntMap.get(scTime) + 1);
            }
        }

        for (Map.Entry<Long, Integer> entry : cntMap.entrySet()) {
            long key = entry.getKey();
            int count = entry.getValue();
            cumulMap.put(key, cumulMap.get(key) / count);
        }
        return cumulMap;
    }

    public Map<Long, Integer> getFrequencyPerDay() {
        return cntMap;
    }
}
