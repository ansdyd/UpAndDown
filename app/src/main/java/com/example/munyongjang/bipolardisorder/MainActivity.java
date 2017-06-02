package com.example.munyongjang.bipolardisorder;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String ORIGIN_NUMBER = "1111111111";

    public GoogleApiClient mApiClient;

    private ActivityScoreDatabaseHandler activityDBHandler;
    private LineChart mActivityChart;
    private SentimentScoreDatabaseHandler ssDB;
    private LineChart mSentimentChart;

    private Toolbar mToolBar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // activity recognition
        initializeDataBaseHandler();
        initializeActivityAPI();
        // set up the basic UI for Physical Activity
        setUpActivityChart();

        // sms stuff
        getSMS();
        // set up the basic UI for text sentiment MPAndroid
        setUpSentimentChart();

        // for the toolbar
        mToolBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolBar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open
                , R.string.close);
        mDrawerLayout.setDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.activity_sms_graph_page) {
                    mDrawerLayout.closeDrawers();
                } else if (id == R.id.phase_log_page) {
                    // NOTE: DO NOT CALL FINISH ON MAIN ACTIVITY
                    Log.d("log page", "log page clicked!");
                    mDrawerLayout.closeDrawers();
                    startActivity(new Intent(MainActivity.this, LogActivity.class));
                }
                return false;
            }
        });
        EventBus.getDefault().postSticky(new EpisodeLogDatabaseHandler(this));
    }

    private void setUpSentimentChart() {
        mSentimentChart = (LineChart) findViewById(R.id.chart2);
        mSentimentChart.getDescription().setEnabled(false);
        //touch gestures
        mSentimentChart.setTouchEnabled(true);
        mSentimentChart.setDragDecelerationFrictionCoef(0.9f);
        //scaling and dragging
        mSentimentChart.setDragEnabled(true);
        mSentimentChart.setScaleEnabled(true);
        mSentimentChart.setDrawGridBackground(false);
        mSentimentChart.setHighlightPerDragEnabled(true);
        mSentimentChart.setBackgroundColor(Color.WHITE);
        mSentimentChart.setViewPortOffsets(0f, 0f, 0f, 0f);
        setSentimentData();
        mSentimentChart.invalidate();
        mSentimentChart.getLegend().setEnabled(false);

        XAxis xAxis = mSentimentChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTypeface(Typeface.SANS_SERIF);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one day
        xAxis.setAvoidFirstLastClipping(true);
        // not sure if this is the right way
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd");
            //TODO: when this runs into other problems, fix it more robustly
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                Calendar temp = Calendar.getInstance();
                temp.setTimeInMillis(millis);
                // this is a temporary fix
                temp.add(Calendar.HOUR_OF_DAY, 5);
                return mFormat.format(temp.getTime());
            }
        });

        //YAxis
        YAxis leftAxis = mSentimentChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(1f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mSentimentChart.getAxisRight();
        rightAxis.setEnabled(false);
        mSentimentChart.invalidate();
    }

    // basically the same as set activity data
    private void setSentimentData() {
        Map<Long, Double> avgMap = ssDB.getAveragePerDay();
        ArrayList<Entry> values = new ArrayList<>();
        long[] keySet = new long[avgMap.keySet().size()];
        int index = 0;
        for (long key : avgMap.keySet()) {
            keySet[index] = key;
            index++;
        }
        // sort it according to date
        Arrays.sort(keySet);

        for(long key : keySet) {
            float val = (float) ((double) avgMap.get(key));
            //Log.d("longKey", "" + TimeUnit.MILLISECONDS.toDays(key));
            Date test = new Date(key);
            Log.d("avgSentiment", "year: " + test.getYear() +" month: "+ test.getMonth() + " day: "
                    + test.getDate() + " score: " + val);
            values.add(new Entry(TimeUnit.MILLISECONDS.toDays(key), val));
        }

        // TODO: change colors + do away with temporary fix
        if (avgMap.size() == 0) {
            values.add(new Entry(0, 0));
        }

        LineDataSet set1 = new LineDataSet(values, "Sentiment_Data");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(true);
        set1.setDrawValues(true);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(true);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);
        mSentimentChart.setData(data);
    }

    // this may be giving me an error
    private void getSMS() {
        ssDB = new SentimentScoreDatabaseHandler(this);
        // inbox
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String sender = cursor.getString(2);
                long date = cursor.getLong(4);
                if (!ssDB.containsSentimentInformation(date, sender)) {
                    Log.d("sms", "new incoming one!");

                    // text to be analyzed, currently using Google
                    String text = cursor.getString(cursor.getColumnIndex("body"));
                    new Google().execute(text, ssDB, date, sender);

                } else {
                    // no need to go down further since this is in reverse order (LIFO)
                    break;
                }
            } while (cursor.moveToNext());
        }

        //outbox
        cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String sender = ORIGIN_NUMBER;
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                String text = cursor.getString(cursor.getColumnIndex("body"));

                if(!ssDB.containsSentimentInformation(date, sender)) {
                    Log.d("sms", "new outgoing one!");
                    new Google().execute(text, ssDB, date, sender);
                } else {
                    break;
                }
            } while (cursor.moveToNext());
        }
    }

    // to initializae the Activity Chart
    private void setUpActivityChart() {
        mActivityChart = (LineChart) findViewById(R.id.chart1);
        mActivityChart.getDescription().setText("");
        //touch gestures
        mActivityChart.setTouchEnabled(true);
        mActivityChart.setDragDecelerationFrictionCoef(0.9f);
        //scaling and dragging
        mActivityChart.setDragEnabled(true);
        mActivityChart.setScaleEnabled(true);
        mActivityChart.setDrawGridBackground(false);
        mActivityChart.setHighlightPerDragEnabled(true);

        mActivityChart.setBackgroundColor(Color.WHITE);
        mActivityChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // setting the activity data on the chart
        setActivityData();
        mActivityChart.invalidate();
        mActivityChart.getLegend().setEnabled(false);

        XAxis xAxis = mActivityChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setTypeface(Typeface.SANS_SERIF);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one day
        xAxis.setAvoidFirstLastClipping(true);

        // not sure if this is the right way
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd");

            //TODO: when this runs into other problems, fix it more robustly
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = TimeUnit.DAYS.toMillis((long) value);
                //Log.d("dateDebug", new Date(millis).toString());
                Calendar temp = Calendar.getInstance();
                temp.setTimeInMillis(millis);

                // this is a temporary fix
                temp.add(Calendar.HOUR_OF_DAY, 5);
                return mFormat.format(temp.getTime());
            }
        });

        //YAxis
        YAxis leftAxis = mActivityChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(9f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mActivityChart.getAxisRight();
        rightAxis.setEnabled(false);
        mActivityChart.invalidate();
    }

    // setting the data on the activity Chart
    private void setActivityData() {
        Map<Long, Double> avgMap = activityDBHandler.getAveragePerDay();
        ArrayList<Entry> values = new ArrayList<>();
        long[] keySet = new long[avgMap.keySet().size()];
        int index = 0;
        for (long key : avgMap.keySet()) {
            keySet[index] = key;
            index++;
        }
        // sort it according to date
        Arrays.sort(keySet);

        for(long key : keySet) {
            float val = (float) ((double) avgMap.get(key));
            Log.d("longKey", "" + TimeUnit.MILLISECONDS.toDays(key));
            Date test = new Date(key);
            Log.d("avgTime", "year: " + test.getYear() +" month: "+ test.getMonth() + " day: " + test.getDate());
            values.add(new Entry(TimeUnit.MILLISECONDS.toDays(key), val));
        }

        LineDataSet set1 = new LineDataSet(values, "Activity_Data");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(true);
        set1.setDrawValues(true);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(true);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);
        mActivityChart.setData(data);
    }

    private void initializeDataBaseHandler() {
        activityDBHandler = new ActivityScoreDatabaseHandler(this);
        // to pass the database handler to the intent service
        EventBus.getDefault().postSticky(activityDBHandler);
    }

    // to initialize activity tracking
    private void initializeActivityAPI() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mApiClient.connect();
    }

    // the callbacks needed for the activity recognizer
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("connected", "just connected to google play services api");
        Intent intent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // this sets up the sampling frequency
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient,
                1000, pendingIntent);
    }

    // for the adding button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.phase_detection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_episode) {
            Log.d("toast", "adding an episode selected");
            startActivity(new Intent(MainActivity.this, EpisodeActivity.class));
        }
        else if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // not sure what this does
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
