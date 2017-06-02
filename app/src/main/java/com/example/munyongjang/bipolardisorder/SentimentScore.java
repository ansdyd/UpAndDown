package com.example.munyongjang.bipolardisorder;

/**
 * Created by munyongjang on 3/22/17.
 */

// microsoft's score will have a magntiude of -1 since Google's ranges from 0 to POS_INFINITY
public class SentimentScore {
    private double score;
    private double magnitude;
    private long time;
    private String sender;

    public SentimentScore() {}
    public SentimentScore(long time, double score, double magnitude, String sender) {
        this.score = score;
        this.magnitude = magnitude;
        this.time = time;
        this.sender = sender;
    }
    public String getSender() {
        return sender;
    }
    public double getScore() {
        return score;
    }
    public double getMagnitude() {
        return magnitude;
    }
    public long getTime() {
        return time;
    }

    public void setScore(double score) {
        this.score = score;
    }
    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

}
