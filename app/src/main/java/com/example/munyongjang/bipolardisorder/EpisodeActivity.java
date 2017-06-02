package com.example.munyongjang.bipolardisorder;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by munyongjang on 3/24/17.
 */

public class EpisodeActivity extends AppCompatActivity {

    private CalendarPickerView calendarPickerView;
    private Toolbar mToolBar;
    private EpisodeLogDatabaseHandler mELDH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_episode);

        // putting the mini toolbar now
        mToolBar = (Toolbar) findViewById(R.id.add_episode_bar);
        setSupportActionBar(mToolBar);

        // making the "pop-up feel"
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (0.8 * width), (int) (height* 0.8));

        // dates can be picked from 4 months before to today
        Calendar prevYear = Calendar.getInstance();
        prevYear.add(Calendar.MONTH, -4);
        calendarPickerView = (CalendarPickerView) findViewById(R.id.calendar_view);
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_YEAR, 1);
        calendarPickerView.init(prevYear.getTime(),
                today.getTime()).inMode(CalendarPickerView.SelectionMode.RANGE);
        mELDH = EventBus.getDefault().getStickyEvent(EpisodeLogDatabaseHandler.class);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.add_episode_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_time_frame) {
            // made final due to the inner class
            final List<Date> dates = calendarPickerView.getSelectedDates();

            if (dates.size() == 0) {
                Toast.makeText(this, "Must Pick a Date Range!", Toast.LENGTH_SHORT).show();
            } else {
                View menuItemView = findViewById(R.id.action_add_time_frame);
                PopupMenu popupMenu = new PopupMenu(this, menuItemView);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        List<Date> dates = calendarPickerView.getSelectedDates();
                        Date startDate = dates.get(0);
                        Date endDate = dates.get(dates.size() - 1);
                        if (id == R.id.action_add_depressed_phase) {
                            // "D" is for depressed
                            mELDH.insertEpisode(startDate.getTime(), endDate.getTime(), "D");
                            Toast.makeText(EpisodeActivity.this, "Adding Depressed Phase",
                                    Toast.LENGTH_SHORT).show();
                        } else if (id == R.id.action_add_manic_phase) {
                            // "M" is for manic
                            mELDH.insertEpisode(startDate.getTime(), endDate.getTime(), "M");
                            Toast.makeText(EpisodeActivity.this, "Adding (Hypo)Manic Phase",
                                    Toast.LENGTH_SHORT).show();
                        }
                        Log.d("Phase Input", "Beginning Date: " + dates.get(0).toString() +
                                " Ending Date: " + dates.get(dates.size() - 1).toString());

                        // posting onto eventbus before closing out the activity
                        EventBus.getDefault().postSticky(mELDH);
                        finish();
                        return true;
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
