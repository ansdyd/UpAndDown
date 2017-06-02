package com.example.munyongjang.bipolardisorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by munyongjang on 3/10/17.
 */


// for taking care of the ActivityScore database
public class ActivityScoreDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BipolarDisorderApplicationDataBase";
    private static final String TABLE_ACTIVITIES = "activities";

    private static final String KEY_TIME = "time";
    private static final String KEY_ID = "id";
    private static final String KEY_SCORE = "score";

    // seems like nothing gets created..
    private SQLiteDatabase db;

    public ActivityScoreDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public ActivityScoreDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("ACDH", "Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("ACDH", "onCreate");
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ACTIVITIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                + KEY_SCORE + " INTEGER" + ")";

        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(db);
    }

    /*
    All CRUD methods here
     */
    public void addActivityScore(ActivityScore activityScore) {
        Log.d("add_Activity", "" + activityScore.getMsTime());

        SQLiteDatabase db = this.getWritableDatabase();

        // putting the time and the score
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, activityScore.getMsTime());
        values.put(KEY_SCORE, activityScore.getScore());

        db.insert(TABLE_ACTIVITIES, null, values);
        db.close();
    }

    public List<ActivityScore> getAllActivityScore() {
        List<ActivityScore> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ACTIVITIES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ActivityScore activityScore = new ActivityScore();

                // this seems to be working now
                activityScore.setMsTime(cursor.getLong(1));
                activityScore.setScore(cursor.getInt(2));

                list.add(activityScore);

            } while(cursor.moveToNext());
        }

        return list;
    }

    // function to return all the averages per day for the data collected
    // returns YEAR + DAY OF YEAR as a concatenated string
    public Map<Long, Double> getAveragePerDay() {

        Map<Long, Double> averageMap = new HashMap<>();
        List<ActivityScore> activityScoreList = getAllActivityScore();
        // calculation to get the averages for each day
        Date prev = null;
        double score = 0;
        int count = 0;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        for (ActivityScore ac : activityScoreList) {
            Date curDate = new Date(ac.getMsTime());
            if (prev != null) {
                cal1.setTime(prev);
                cal2.setTime(curDate);

                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                // could get a bit tricky with how to save it into the hashmap
                if (!sameDay) {
                    //long key = prev.getTime();
                    Log.d("ASDHDifDay", "year: "+ cal1.get(Calendar.YEAR) + " month: "
                            + cal1.get(Calendar.MONTH) + " day: " + cal1.get(Calendar.DAY_OF_MONTH));

                    cal1.set(Calendar.HOUR_OF_DAY, 0);
                    cal1.set(Calendar.MINUTE, 0);
                    cal1.set(Calendar.SECOND, 0);
                    cal1.set(Calendar.MILLISECOND, 0);

                    Double avg = (double) score / count;
                    averageMap.put(cal1.getTimeInMillis(), avg);
                    score = 0;
                    count = 0;
                }
            }
            prev = curDate;
            score += ac.getScore();
            count++;
        }
        // last one
        if (prev != null) {
            cal1.setTime(prev);
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
            averageMap.put(cal1.getTimeInMillis(), (double) score / count);
        }

        return averageMap;
    }
}
