/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.ui.activities.MainActivity;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;

/**
 * Edited by iClaude on 25/09/2017.
 * Service used to record audio. This class implements an hybrid Service (bound and started
 * Service).
 * Compared with the original Service, this class adds 2 new features:
 * 1) record scheduled recordings
 * 2) bound Service features to connect this Service to an Activity
 */

public class RecordingService extends Service {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String EXTRA_ACTIVITY_STARTER = "com.danielkim.soundrecorder.EXTRA_ACTIVITY_STARTER";
    private static final int ONGOING_NOTIFICATION = 1;

    @Inject
    RecordingsRepository recordingsRepository;

    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private long mStartingTimeMillis = 0;
    private int mElapsedSeconds = 0;

    private TimerTask mIncrementTimerTask = null;

    private final IBinder myBinder = new LocalBinder();
    private boolean isRecording = false;

    // Just for testing.
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int onCreateCalls = 0;
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int onDestroyCalls = 0;
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static int onStartCommandCalls = 0;

    /*
        Static factory method used to create an Intent to start this Service. The boolean value
        activityStarter is true if this method is called by an Activity, false otherwise (i.e.
        Service started by an AlarmManager for a scheduled recording).
    */
    public static Intent makeIntent(Context context, boolean activityStarter) {
        Intent intent = new Intent(context.getApplicationContext(), RecordingService.class);
        intent.putExtra(EXTRA_ACTIVITY_STARTER, activityStarter);
        return intent;
    }

    /*
        The following code implements a bound Service used to connect this Service to an Activity.
    */
    public class LocalBinder extends Binder {
        public RecordingService getService() {
            return RecordingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    /*
        Interface used to communicate to a connected component changes in the status of a
        recording:
        - recording started
        - recording stopped (with file path)
        - seconds elapsed
     */
    public interface OnRecordingStatusChangedListener {
        void onRecordingStarted();
        void onTimerChanged(int seconds);
        void onRecordingStopped(String filePath);
    }

    private OnRecordingStatusChangedListener onRecordingStatusChangedListener = null;

    public void setOnRecordingStatusChangedListener(OnRecordingStatusChangedListener onRecordingStatusChangedListener) {
        this.onRecordingStatusChangedListener = onRecordingStatusChangedListener;
    }

    /*
        The following code implements a started Service.
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartCommandCalls++;
        boolean activityStarter = intent.getBooleanExtra(EXTRA_ACTIVITY_STARTER, false);
        if (!activityStarter) { // automatic scheduled recording
            // Get next recording data.
            recordingsRepository.getNextScheduledRecording(
                    new RecordingsRepositoryInterface.GetScheduledRecordingCallback() {
                        @Override
                        public void onSuccess(ScheduledRecording recording) {
                            int duration = (int) (recording.getEnd() - recording.getStart());
                            // Remove scheduled recording from database and schedule next recording.
                            recordingsRepository.deleteScheduledRecording(recording,null);
                            startService(ScheduledRecordingService.makeIntent(RecordingService.this, false));

                            if (!isRecording && hasPermissions()) {
                                startRecording(duration);
                            }
                        }

                        @Override
                        public void onFailure() {
                            Log.e(TAG, CLASS_NAME + " - getNextScheduledRecording(): " + "error in retrieving next scheduled recording");
                        }
                    }
            );
        }

        return START_NOT_STICKY;
    }

    /*
        The following code is shared by both started and bound Service.
     */

    @Override
    public void onCreate() {
        onCreateCalls++;
        super.onCreate();
        App.getComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        onDestroyCalls++;
        super.onDestroy();
        if (mRecorder != null) {
            stopRecording();
        }

        if (onRecordingStatusChangedListener != null) onRecordingStatusChangedListener = null;
    }

    public void startRecording(int duration) {
        startForeground(ONGOING_NOTIFICATION, createNotification());

        setFileNameAndPath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setMaxDuration(duration); // if this is a scheduled recording, set the max duration, after which the Service is stopped
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }
        // Called only if a max duration has been set (scheduled recordings).
        mRecorder.setOnInfoListener((mediaRecorder, what, extra) -> {
            if (what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecording();
            }
        });

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
            isRecording = true;

            startTimer();
        } catch (IOException e) {
            Log.e(TAG, CLASS_NAME + " - startRecording(): " + "prepare() failed" + e.toString());
        }

        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener.onRecordingStarted();
        }
    }

    private void setFileNameAndPath() {
        mFileName = "rec" + System.currentTimeMillis();
        mFilePath = Utils.getDirectoryPath(this) + "/" + mFileName;
    }

    private void startTimer() {
        Timer mTimer = new Timer();
        mElapsedSeconds = 0;
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                if (onRecordingStatusChangedListener != null) {
                    onRecordingStatusChangedListener.onTimerChanged(mElapsedSeconds);
                }
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    public void stopRecording() {
        mRecorder.stop();
        long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        isRecording = false;
        mRecorder = null;

        // Communicate the file path to the connected Activity.
        if (onRecordingStatusChangedListener != null) {
            onRecordingStatusChangedListener.onRecordingStopped(mFilePath);
        }


        // Save the recording data in the database.
        Recording recording = new Recording(mFileName, mFilePath, mElapsedMillis, System.currentTimeMillis());
        recordingsRepository.insertRecording(recording, new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                Log.i(TAG, CLASS_NAME + " - stopRecording(): " + "recording added to database");
            }

            @Override
            public void onFailure() {
                Log.e(TAG, CLASS_NAME + " - stopRecording(): " + "error in adding recording to database");
            }
        });

        // Stop timer.
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        // No Activity connected -> stop the Service (scheduled recording).
        if (onRecordingStatusChangedListener == null)
            stopSelf();

        stopForeground(true);
    }

    private Notification createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(getString(R.string.notification_recording_text))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, PendingIntent.FLAG_UPDATE_CURRENT));

        return mBuilder.build();
    }

    public boolean isRecording() {
        return isRecording;
    }

    /*
        For Marshmallow+ check if we have the necessary permissions. This method is called for
        scheduled recordings because the use might deny the permissions after a scheduled
        recording has already been set.
     */
    private boolean hasPermissions() {
        boolean writePerm = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean audioPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        return writePerm && audioPerm;
    }
}
