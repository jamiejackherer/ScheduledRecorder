/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.database;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.utils.AppExecutors;

import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Access point to the database.
 */

public class RecordingsRepository implements RecordingsRepositoryInterface {

    @Inject
    RecordingsDao recordingsDao;
    @Inject
    AppExecutors appExecutors;

    public RecordingsRepository(@NonNull AppExecutors appExecutors, @NonNull RecordingsDao recordingsDao) {
        App.getComponent().inject(this);
    }

    // Table "saved_recordings".

    @Override
    public void insertRecording(Recording recording, OperationResult callback) {
        checkNotNull(recording);
        Runnable insertRunnable = () -> {
            long id = recordingsDao.insertRecording(recording);

            appExecutors.mainThread().execute(() -> {
                if (id > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(insertRunnable);
    }

    @Override
    public void updateRecordings(OperationResult callback, Recording... recordings) {
        checkNotNull(recordings);
        Runnable updateRunnable = () -> {
            int num = recordingsDao.updateRecordings(recordings);

            appExecutors.mainThread().execute(() -> {
                if (num > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(updateRunnable);
    }

    @Override
    public void deleteRecordings(OperationResult callback, Recording... recordings) {
        checkNotNull(recordings);
        Runnable deleteRunnable = () -> {
            int num = recordingsDao.deleteRecordings(recordings);

            appExecutors.mainThread().execute(() -> {
                if (num > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void getRecordingById(int id, GetRecordingCallback callback) {
        Runnable runnable = () -> {
            final Recording recording = recordingsDao.getRecordingById(id);

            appExecutors.mainThread().execute(() -> {
                if (recording != null) {
                    callback.onSuccess(recording);
                } else {
                    callback.onFailure();
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public LiveData<List<Recording>> getAllRecordings() {
        return recordingsDao.getAllRecordings();
    }

    @Override
    public void getRecordingsCount(GetRecordingsCountCallback callback) {
        Runnable runnable = () -> {
            final int count = recordingsDao.getRecordingsCount();

            appExecutors.mainThread().execute(() -> callback.recordingsCount(count));
        };

        appExecutors.diskIO().execute(runnable);
    }

    // Table "scheduled_recordings".
    @Override
    public void insertScheduledRecording(ScheduledRecording recording, OperationResult callback) {
        checkNotNull(recording);
        Runnable insertRunnable = () -> {
            long id = recordingsDao.insertScheduledRecording(recording);

            appExecutors.mainThread().execute(() -> {
                if (id > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(insertRunnable);
    }

    @Override
    public void updateScheduledRecordings(OperationResult callback, ScheduledRecording... recordings) {
        checkNotNull(recordings);
        Runnable updateRunnable = () -> {
            int num = recordingsDao.updateScheduledRecordings(recordings);

            appExecutors.mainThread().execute(() -> {
                if (num > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(updateRunnable);
    }

    @Override
    public void deleteScheduledRecordings(OperationResult callback, ScheduledRecording... recordings) {
        checkNotNull(recordings);
        Runnable deleteRunnable = () -> {
            int num = recordingsDao.deleteScheduledRecordings(recordings);

            appExecutors.mainThread().execute(() -> {
                if (callback == null) return;

                if (num > 0)
                    callback.onSuccess();
                else
                    callback.onFailure();
            });
        };
        appExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void getScheduledRecordingById(int id, GetScheduledRecordingCallback callback) {
        Runnable runnable = () -> {
            final ScheduledRecording recording = recordingsDao.getScheduledRecordingById(id);

            appExecutors.mainThread().execute(() -> {
                if (recording != null) {
                    callback.onSuccess(recording);
                } else {
                    callback.onFailure();
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public LiveData<List<ScheduledRecording>> getAllScheduledRecordings() {
        return recordingsDao.getAllScheduledRecordings();
    }

    @Override
    public void getScheduledRecordingsCount(GetRecordingsCountCallback callback) {
        Runnable runnable = () -> {
            final int count = recordingsDao.getScheduledRecordingsCount();

            appExecutors.mainThread().execute(() -> callback.recordingsCount(count));
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getNextScheduledRecording(GetScheduledRecordingCallback callback) {
        Runnable runnable = () -> {
            final ScheduledRecording recording = recordingsDao.getNextScheduledRecording();

            appExecutors.mainThread().execute(() -> {
                if (recording != null) {
                    callback.onSuccess(recording);
                } else {
                    callback.onFailure();
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getNumRecordingsAlreadyScheduled(long start, long end, int exceptId, GetRecordingsCountCallback callback) {
        Runnable runnable = () -> {
            final int count = recordingsDao.getNumRecordingsAlreadyScheduled(start, end, exceptId);

            appExecutors.mainThread().execute(() -> callback.recordingsCount(count));
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getScheduledRecordingsBetween(long start, long end, GetScheduledRecordingsCallback callback) {
        Runnable runnable = () -> {
            final List<ScheduledRecording> recordings = recordingsDao.getScheduledRecordingsBetween(start, end);

            appExecutors.mainThread().execute(() -> {
                if (recordings != null) {
                    callback.onSuccess(recordings);
                } else {
                    callback.onFailure();
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }
}
