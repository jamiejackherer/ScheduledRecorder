package com.iclaude.scheduledrecorder.testutils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.iclaude.scheduledrecorder.ScheduledRecordingService;

/**
 * Created by iClaude on 26/07/2017.
 * This is a mock class of ScheduledRecordingService created to test the service with
 * Robolectric.
 * In this mock class you provide a Context and an AlarmManager through the constructor. The
 * alarms are set in the main thread, while in the original service they are set in a
 * background thread.
 */

public class MockScheduledRecordingService extends ScheduledRecordingService {

    public MockScheduledRecordingService(Context context, AlarmManager alarmManager) {
        this.context = context;
        this.alarmManager = alarmManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartCommandCalls++; // just for testing

        resetAlarmManager(); // cancel all pending alarms
        scheduleNextRecording();

        return START_REDELIVER_INTENT;
    }
}
