/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;

import java.util.Objects;

import javax.inject.Inject;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.GetScheduledRecordingCallback;

/**
 * This Service gets triggered at boot time and sets the next scheduled recording using an
 * AlarmManager. Scheduled recordings are retrieved from the database and loaded in a separate
 * thread.
 * This class (started Service) also implements the Local Binder pattern just for testing purposes.
 */
public class ScheduledRecordingService extends Service implements Handler.Callback {

    private final int SCHEDULE_RECORDINGS = 1;
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final int NOTIFICATION_SCHEDULING = 0;
    protected static final String EXTRA_WAKEFUL = "com.danielkim.soundrecorder.WAKEFUL";

    @Inject
    RecordingsRepository recordingsRepository;

    protected AlarmManager alarmManager;
    protected Context context;
    private Handler mHandler;

    // Just for testing.
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int onCreateCalls, onDestroyCalls, onStartCommandCalls;
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    private final LocalBinder localBinder = new LocalBinder();

    /*
        Static factory method used to create an Intent to start this Service.
    */
    public static Intent makeIntent(Context context) {
        return new Intent(context, ScheduledRecordingService.class);
    }

    public ScheduledRecordingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.getComponent().inject(this);

        onCreateCalls++; // just for testing

        if (alarmManager == null)
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (context == null) context = this;

        // Start background thread with a Looper.
        HandlerThread handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onDestroyCalls++; // just for testing

        // Stop background thread.
        mHandler.getLooper().quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartCommandCalls++; // just for testing

        startForeground(NOTIFICATION_SCHEDULING, null);

        Message message = mHandler.obtainMessage(SCHEDULE_RECORDINGS);
        mHandler.sendMessage(message);

        return START_REDELIVER_INTENT;
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == SCHEDULE_RECORDINGS) {
            resetAlarmManager(); // cancel all pending alarms
            deleteOldRecordingsAndScheduleNext();
        }

        return true;
    }

    // Cancels all pending alarms already set in the AlarmManager.
    protected void resetAlarmManager() {
        Intent intent = new Intent(context, RecordingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    // Delete expired scheduled recordings from database.
    protected void deleteOldRecordingsAndScheduleNext() {
        recordingsRepository.deleteOldScheduledRecordings(System.currentTimeMillis() - 1000*60*5, new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                scheduleNextRecording();
            }

            @Override
            public void onFailure() {
            }
        });
    }

    // Get scheduled recordings from database and set the AlarmManager.
    protected void scheduleNextRecording() {
        GetScheduledRecordingCallback callback = new GetScheduledRecordingCallback() {
            @Override
            public void onSuccess(ScheduledRecording recording) {
                if (recording != null) {
                    Intent intent = RecordingService.makeIntent(context, false);

                    PendingIntent pendingIntent;
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                        pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    else
                        pendingIntent = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)  // API 19-22
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, recording.getStart(), pendingIntent);
                     else  // API 23+
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, recording.getStart(), pendingIntent);

                }

                stopForeground(false);
            }

            @Override
            public void onFailure() {
                Log.e(TAG, getClass().getSimpleName() + " - scheduleNextRecording(): error in getting the next scheduled recording");
                stopForeground(false);
            }
        };

        recordingsRepository.getNextScheduledRecording(callback);
    }

    private Notification createNotification() {
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = "";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_scheduling))
                        .setContentText(getString(R.string.notification_scheduling_text))
                        .setOngoing(true);

        return mBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        String channelId = "scheduled_recording_service";
        String channelName = "Scheduled Recording Service";
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).createNotificationChannel(chan);
        return channelId;
    }


    /*
        Implementation of local binder pattern for testing purposes.
    */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public class LocalBinder extends Binder {
        public ScheduledRecordingService getService() {
            return ScheduledRecordingService.this;
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

}
