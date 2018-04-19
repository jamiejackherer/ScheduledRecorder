/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

/*
 * Year: 2017. This class was edited by iClaude.
 */

package com.iclaude.scheduledrecorder.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.RecordingService;
import com.iclaude.scheduledrecorder.ui.fragments.fileviewer.FileViewerFragment;
import com.iclaude.scheduledrecorder.ui.fragments.record.RecordFragment;
import com.iclaude.scheduledrecorder.ui.fragments.record.RecordViewModel;
import com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings.ScheduledRecordingsFragment;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private RecordViewModel recordViewModel; // manages connection with RecordingService


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPagerNoSwipe pager = findViewById(R.id.pager);
        pager.setSwipingEnabled(false);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        setSupportActionBar(toolbar);

        recordViewModel = ViewModelProviders.of(this).get(RecordViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class MyAdapter extends FragmentPagerAdapter {
        private final String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings),
                getString(R.string.tab_title_scheduled_recordings)};

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:{
                    return RecordFragment.newInstance(position);
                }
                case 1:{
                    return FileViewerFragment.newInstance(position);
                }
                case 2: {
                    return ScheduledRecordingsFragment.newInstance(position);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public MainActivity() {
    }

    // Connection with local Service through the view model.
    @Override
    protected void onStart() {
        super.onStart();

        recordViewModel.connectService(RecordingService.makeIntent(this, true));
    }

    // Disconnection from local Service.
    @Override
    protected void onStop() {
        super.onStop();

        recordViewModel.disconnectAndStopService(new Intent(this, RecordingService.class));
    }
}
