/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.ScheduledRecordingService;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.ui.fragments.DatePickerFragment;
import com.iclaude.scheduledrecorder.ui.fragments.DatePickerFragment.MyOnDateSetListener;
import com.iclaude.scheduledrecorder.ui.fragments.TimePickerFragment;
import com.iclaude.scheduledrecorder.ui.fragments.TimePickerFragment.MyOnTimeSetListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

import static com.iclaude.scheduledrecorder.database.RecordingsContract.MAX_DURATION;
import static com.iclaude.scheduledrecorder.database.RecordingsContract.MIN_DURATION;
import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;

/**
 * Activity used to add a new scheduled recording.
 */

public class AddScheduledRecordingActivity extends AppCompatActivity implements MyOnDateSetListener, MyOnTimeSetListener {
    private enum Operation {ADD, EDIT}

    private interface StatusCodes {
        int NO_ERROR = 0;
        int ERROR_START_AFTER_END = 1;
        int ERROR_TIME_PAST = 2;
        int ERROR_ALREADY_SCHEDULED = 3;
        int ERROR_SAVING = 4;
    }

    private static final String EXTRA_DATE_LONG = "com.danielkim.soundrecorder.activities.EXTRA_DATE_LONG";
    private static final String EXTRA_ITEM = "com.danielkim.soundrecorder.activities.EXTRA_ITEM";

    private TextView tvDateStart;
    private TextView tvDateEnd;
    private TextView tvTimeStart;
    private TextView tvTimeEnd;

    private Operation operation;
    private ScheduledRecording item = null;
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private int yearStart, monthStart, dayStart, hourStart, minuteStart;
    private int yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd;
    private int statusCode = StatusCodes.NO_ERROR;
    private static final int[] toastMsgs = {R.string.toast_scheduledrecording_saved,
            R.string.toast_scheduledrecording_timeerror_start_after_end, R.string.toast_scheduledrecording_timeerror_past,
            R.string.toast_scheduledrecording_timeerror_already_scheduled, R.string.toast_scheduledrecording_saved_error};

    @Inject
    RecordingsRepository recordingsRepository;
    private Disposable disposable;

    public static Intent makeIntent(Context context, long selectedDate) {
        Intent intent = new Intent(context, AddScheduledRecordingActivity.class);
        intent.putExtra(EXTRA_DATE_LONG, selectedDate);
        return intent;
    }

    public static Intent makeIntent(Context context, ScheduledRecording item) {
        Intent intent = new Intent(context, AddScheduledRecordingActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduled_recording);

        App.getComponent().inject(this);
        // Action bar (Toolbar).
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false); // hide the title
            ab.setDisplayHomeAsUpEnabled(true);
        }


        tvDateStart = findViewById(R.id.tvDateStart);
        tvDateEnd = findViewById(R.id.tvDateEnd);
        tvTimeStart = findViewById(R.id.tvTimeStart);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);

        long selectedDateLong = getIntent().getLongExtra(EXTRA_DATE_LONG, System.currentTimeMillis());
        item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (item == null) {
            initVariables(selectedDateLong);
        } else {
            initVariables(item);
        }
        checkDatesAndTimes();
        displayDatesAndTimes();

    }

    // Initialize starting and ending days and times (operation = add).
    private void initVariables(long selectedDateLong) {
        operation = Operation.ADD;

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(selectedDateLong);
        yearStart = yearEnd = cal.get(Calendar.YEAR);
        monthStart = monthEnd = cal.get(Calendar.MONTH);
        dayStart = dayEnd = cal.get(Calendar.DAY_OF_MONTH);
        hourStart = 0;
        minuteStart = 0;
        hourEnd = 1;
        minuteEnd = 0;
    }

    // Initialize starting and ending days and times (operation = edit).
    private void initVariables(@NonNull ScheduledRecording item) {
        operation = Operation.EDIT;

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(item.getStart());
        yearStart = cal.get(Calendar.YEAR);
        monthStart = cal.get(Calendar.MONTH);
        dayStart = cal.get(Calendar.DAY_OF_MONTH);
        hourStart = cal.get(Calendar.HOUR_OF_DAY);
        minuteStart = cal.get(Calendar.MINUTE);

        cal.setTimeInMillis(item.getEnd());
        yearEnd = cal.get(Calendar.YEAR);
        monthEnd = cal.get(Calendar.MONTH);
        dayEnd = cal.get(Calendar.DAY_OF_MONTH);
        hourEnd = cal.get(Calendar.HOUR_OF_DAY);
        minuteEnd = cal.get(Calendar.MINUTE);
    }

    // When dates and times change, display them again.
    private void displayDatesAndTimes() {
        tvDateStart.setText(dateFormat.format(new Date(new GregorianCalendar(yearStart, monthStart, dayStart).getTimeInMillis())));
        tvDateEnd.setText(dateFormat.format(new Date(new GregorianCalendar(yearEnd, monthEnd, dayEnd).getTimeInMillis())));
        tvTimeStart.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", hourStart, minuteStart));
        tvTimeEnd.setText(String.format(Locale.getDefault(), "%1$02d:%2$02d", hourEnd, minuteEnd));
    }

    public void showDatePickerDialog(View view) {
        DialogFragment datePicker = DatePickerFragment.newInstance(view.getId());
        datePicker.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View view) {
        int hour = 0;
        int minute = 0;
        if (view.getId() == R.id.tvTimeStart) {
            hour = hourStart;
            minute = minuteStart;
        } else if (view.getId() == R.id.tvTimeEnd) {
            hour = hourEnd;
            minute = minuteEnd;
        }

        DialogFragment timePicker = TimePickerFragment.newInstance(view.getId(), hour, minute);
        timePicker.show(getFragmentManager(), "timePicker");
    }

    // Callback methods for DatePickerFragment and TimePickerFragment.
    @Override
    public void onDateSet(long viewId, int year, int month, int day) {
        if (viewId == R.id.tvDateStart) {
            yearStart = year;
            monthStart = month;
            dayStart = day;
        } else if (viewId == R.id.tvDateEnd) {
            yearEnd = year;
            monthEnd = month;
            dayEnd = day;
        }

        checkDatesAndTimes();
        displayDatesAndTimes();
    }

    @Override
    public void onTimeSet(long viewId, int hour, int minute) {
        if (viewId == R.id.tvTimeStart) {
            hourStart = hour;
            minuteStart = minute;
        } else if (viewId == R.id.tvTimeEnd) {
            hourEnd = hour;
            minuteEnd = minute;
        }

        checkDatesAndTimes();
        displayDatesAndTimes();
    }

    private void checkDatesAndTimes() {
        statusCode = getTimeErrorCode();
        if (statusCode != StatusCodes.NO_ERROR) {
            tvDateStart.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvTimeStart.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        } else {
            tvDateStart.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
            tvTimeStart.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
        }
    }

    // Dates and times are correct? What kind of error there is?
    private int getTimeErrorCode() {
        Calendar start = new GregorianCalendar(yearStart, monthStart, dayStart, hourStart, minuteStart);
        Calendar end = new GregorianCalendar(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd);

        if (System.currentTimeMillis() > start.getTimeInMillis()) {
            return StatusCodes.ERROR_TIME_PAST;
        } else if (end.before(start)) {
            return StatusCodes.ERROR_START_AFTER_END;
        } else {
            return StatusCodes.NO_ERROR;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_addscheduledrecording, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveScheduledRecording();
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveScheduledRecording() {
        if (statusCode == StatusCodes.NO_ERROR) {
            saveOrUpdateRecording();
        } else {
            Toast.makeText(this, getString(toastMsgs[statusCode]), Toast.LENGTH_SHORT).show();
        }
    }

    // Save the scheduled recording.
    private int saveOrUpdateRecording() {
        long startLong = new GregorianCalendar(yearStart, monthStart, dayStart, hourStart, minuteStart).getTimeInMillis();
        long endLong = new GregorianCalendar(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd).getTimeInMillis();
        if (endLong - startLong < MIN_DURATION) {
            endLong = startLong + MIN_DURATION; // a scheduled recording must be at least 5 minutes
        } else if (endLong - startLong > MAX_DURATION) {
            endLong = startLong + MAX_DURATION; // a scheduled recording must be at most 3 hours
        }

        if (operation == Operation.ADD) {
            saveRecording(new ScheduledRecording(startLong, endLong));
        } else {
            updateRecording(new ScheduledRecording(item.getId(), startLong, endLong));
        }

        return statusCode;
    }

    private void saveRecording(ScheduledRecording scheduledRecording) {
        recordingsRepository.getNumScheduledRecordingsAtTime(scheduledRecording.getStart(), count -> {
            if (count != 0) {
                statusCode = StatusCodes.ERROR_ALREADY_SCHEDULED;
            } else {
                recordingsRepository.insertScheduledRecording(scheduledRecording, new OperationResult() {
                    @Override
                    public void onSuccess() {
                        saveRecordingCompleted(statusCode);
                    }

                    @Override
                    public void onFailure() {
                        statusCode = StatusCodes.ERROR_SAVING;
                        saveRecordingCompleted(statusCode);

                    }
                });
            }
        });
    }

    private void updateRecording(ScheduledRecording scheduledRecording) {
        recordingsRepository.updateScheduledRecordings(new OperationResult() {
            @Override
            public void onSuccess() {
                saveRecordingCompleted(statusCode);
            }

            @Override
            public void onFailure() {
                statusCode = StatusCodes.ERROR_SAVING;
                saveRecordingCompleted(statusCode);

            }
        }, scheduledRecording);
    }

    private void saveRecordingCompleted(int result) {
        Toast.makeText(this, toastMsgs[result], Toast.LENGTH_SHORT).show();
        if (result == StatusCodes.NO_ERROR) {

            setResult(RESULT_OK);
            startService(ScheduledRecordingService.makeIntent(this, false));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setDatesAndTimesForTesting(int yearStart, int monthStart, int dayStart, int hourStart, int minuteStart, int yearEnd, int monthEnd, int dayEnd, int hourEnd, int minuteEnd) {
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
        this.monthStart = monthStart;
        this.monthEnd = monthEnd;
        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.hourStart = hourStart;
        this.hourEnd = hourEnd;
        this.minuteStart = minuteStart;
        this.minuteEnd = minuteEnd;
        statusCode = getTimeErrorCode();
    }
}