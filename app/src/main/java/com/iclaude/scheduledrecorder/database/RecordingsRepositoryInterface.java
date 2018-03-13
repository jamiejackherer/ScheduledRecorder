/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.database;

import android.arch.lifecycle.LiveData;

import java.util.List;

public interface RecordingsRepositoryInterface {

    interface OperationResult {
        void onSuccess();

        void onFailure();
    }

    interface GetRecordingsCountCallback {
        void recordingsCount(int count);
    }

    // Table "saved_recordings".

    interface GetRecordingCallback {
        void onSuccess(Recording recording);

        void onFailure();
    }

    void insertRecording(Recording recording, OperationResult callback);

    void updateRecordings(OperationResult callback, Recording... recordings);

    void deleteRecordings(OperationResult callback, Recording... recordings);

    void getRecordingById(long id, GetRecordingCallback callback);

    LiveData<List<Recording>> getAllRecordings();

    void getRecordingsCount(GetRecordingsCountCallback callback);

    // Table "scheduled_recordings".

    interface GetScheduledRecordingCallback {
        void onSuccess(ScheduledRecording recording);

        void onFailure();
    }

    interface GetScheduledRecordingsCallback {
        void onSuccess(List<ScheduledRecording> recordings);

        void onFailure();
    }

    void insertScheduledRecording(ScheduledRecording recording, OperationResult callback);

    void updateScheduledRecordings(OperationResult callback, ScheduledRecording... recordings);

    void deleteScheduledRecordings(OperationResult callback, ScheduledRecording... recordings);

    void getScheduledRecordingById(long id, GetScheduledRecordingCallback callback);

    LiveData<List<ScheduledRecording>> getAllScheduledRecordings();

    void getScheduledRecordingsCount(GetRecordingsCountCallback callback);

    void getNextScheduledRecording(GetScheduledRecordingCallback callback);

    void getNumScheduledRecordingsAtTime(long time, GetRecordingsCountCallback callback);

    void getScheduledRecordingsBetween(long start, long end, GetScheduledRecordingsCallback callback);
}
