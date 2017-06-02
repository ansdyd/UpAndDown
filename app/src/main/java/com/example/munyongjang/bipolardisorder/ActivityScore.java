package com.example.munyongjang.bipolardisorder;

/**
 * Created by munyongjang on 3/10/17.
 */


// Skeleton class that just saves information to facilitate the writing and retrival from sqlite db
public class ActivityScore {

    // the time in milliseconds
    private long msTime;
    // the score itself according the activity recognized service
    private int score;

    public ActivityScore(long msTime, int score) {
        this.msTime = msTime;
        this.score = score;
    }

    public ActivityScore() {}

    public long getMsTime() {
        return msTime;
    }

    public int getScore() {
        return score;
    }

    public void setMsTime(long msTime) {
        this.msTime = msTime;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
