package com.example.munyongjang.bipolardisorder;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.EventLog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private EpisodeLogDatabaseHandler mELDH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // preliminary set up
        mToolBar = (Toolbar) findViewById(R.id.action_bar_2);
        setSupportActionBar(mToolBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_log);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open , R.string.close);
        mDrawerLayout.setDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.activity_sms_graph_page) {
                    finish();
                } else if (id == R.id.phase_log_page) {
                    // NOTE: DO NOT CALL FINISH ON MAIN ACTIVITY
                    mDrawerLayout.closeDrawers();
                }
                return false;
            }
        });

        // this is all working correctly
        mELDH = EventBus.getDefault().getStickyEvent(EpisodeLogDatabaseHandler.class);
        List<List<Object>> episodeList = mELDH.getAllEpisodes();

        // to test the episode list view
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        ArrayList<String> dateList = new ArrayList<>();
        ArrayList<Integer> idList = new ArrayList<>();
        for (List<Object> episode : episodeList) {

            String episodeType = ((String)episode.get(2)).equals("D") ? "Depressed" : "(Hypo)manic";

            // this is the "beginning date" of the phase
            dateList.add(df.format(new Date((long) episode.get(0))) + " - " +
                    df.format(new Date((long) episode.get(1))) + " | " + episodeType);
            idList.add((int) episode.get(3));
        }

        MyCustomAdapter customAdapter = new MyCustomAdapter(dateList, idList, this, mELDH);
        ListView lView = (ListView)findViewById(R.id.phase_list_view);
        lView.setAdapter(customAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
