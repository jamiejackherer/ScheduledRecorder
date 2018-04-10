/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.database;

import android.arch.lifecycle.LiveData;
import android.content.Context;

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

    void updateRecording(Recording recording, String newName, Context context, OperationResult callback);

    void deleteRecording(Recording recording, OperationResult callback);

    void deleteAllRecordings();

    void getRecordingById(int id, GetRecordingCallback callback);

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

    void deleteScheduledRecording(ScheduledRecording recording, OperationResult callback);

    void deleteAllScheduledRecordings();

    void deleteOldScheduledRecordings(long time, OperationResult callback);

    void getScheduledRecordingById(int id, GetScheduledRecordingCallback callback);

    LiveData<List<ScheduledRecording>> getAllScheduledRecordings();

    void getScheduledRecordingsCount(GetRecordingsCountCallback callback);

    void getNextScheduledRecording(GetScheduledRecordingCallback callback);

    void getNumRecordingsAlreadyScheduled(long start, long end, int exceptId, GetRecordingsCountCallback callback);

    void getScheduledRecordingsBetween(long start, long end, GetScheduledRecordingsCallback callback);
}
