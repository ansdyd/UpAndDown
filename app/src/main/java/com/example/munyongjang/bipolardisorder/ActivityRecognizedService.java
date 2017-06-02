package com.example.munyongjang.bipolardisorder;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.List;

/**
 * Created by munyongjang on 3/6/17.
 */

/*
    Service for halding activity recognition periodically while the application is in the
    background
 */

public class ActivityRecognizedService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private ActivityScoreDatabaseHandler db;

    public ActivityRecognizedService(String name) {
        super(name);
    }

    public ActivityRecognizedService(ActivityScoreDatabaseHandler db) {
        super("ActivityRecognizedService");
        this.db = db;
    }

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            db = EventBus.getDefault().getStickyEvent(ActivityScoreDatabaseHandler.class);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        // there are already constant values associated with each activity
        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.ON_BICYCLE: {
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you on bike?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());

                        Calendar c = Calendar.getInstance();

                        ActivityScore as = new ActivityScore(c.getTimeInMillis() ,activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.IN_VEHICLE: {
                    if (activity.getConfidence() > 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you in a car?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());

                        Calendar c = Calendar.getInstance();
                        ActivityScore as = new ActivityScore(c.getTimeInMillis() ,activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you running?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());

                        Calendar c = Calendar.getInstance();
                        ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    if (activity.getConfidence() >= 75) {
                        if( activity.getConfidence() >= 75 ) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                            builder.setContentText( "Are you walking?" );
                            builder.setSmallIcon( R.mipmap.ic_launcher );
                            builder.setContentTitle( getString( R.string.app_name ) );
                            NotificationManagerCompat.from(this).notify(0, builder.build());
                            Calendar c = Calendar.getInstance();
                            ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                            db.addActivityScore(as);
                        }
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you on foot?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        Calendar c = Calendar.getInstance();
                        ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.TILTING: {
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you tilting?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        Calendar c = Calendar.getInstance();
                        ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you still?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        Calendar c = Calendar.getInstance();
                        ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                        db.addActivityScore(as);
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                    builder.setContentText( "Unkown Activity" );
                    builder.setSmallIcon( R.mipmap.ic_launcher );
                    builder.setContentTitle( getString( R.string.app_name ) );
                    NotificationManagerCompat.from(this).notify(0, builder.build());
                    Calendar c = Calendar.getInstance();
                    ActivityScore as = new ActivityScore(c.getTimeInMillis(), activity.getType());
                    db.addActivityScore(as);
                    break;
                }
            }
        }
    }
}
