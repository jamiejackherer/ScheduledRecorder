/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.database;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.utils.AppExecutors;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Access point to the database and files.
 */

public class RecordingsRepository implements RecordingsRepositoryInterface {

    @Inject
    RecordingsDao recordingsDao;
    @Inject
    AppExecutors appExecutors;

    public RecordingsRepository() {
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
    public void updateRecording(Recording recording, String newName, Context context, OperationResult callback) {
        checkNotNull(recording);
        Runnable updateRunnable = () -> {
            // Rename the file.
            String newPath = Utils.getDirectoryPath(context);
            newPath += "/" + newName;
            File f = new File(newPath);
            if (f.exists() && !f.isDirectory()) {
                appExecutors.mainThread().execute(callback::onFailure);
                return;
            }

            File oldFilePath = new File(recording.getPath());
            boolean renamed = oldFilePath.renameTo(f);
            if (!renamed) {
                appExecutors.mainThread().execute(callback::onFailure);
                return;
            }

            // Update the database.
            Recording updatedRecording = new Recording(recording.getId(), newName, newPath, recording.getLength(), recording.getTimeAdded());
            int num = recordingsDao.updateRecording(updatedRecording);
            appExecutors.mainThread().execute(() -> {
                if (num > 0)
                    appExecutors.mainThread().execute(callback::onSuccess);
                else
                    appExecutors.mainThread().execute(callback::onFailure);
            });
        };
        appExecutors.diskIO().execute(updateRunnable);
    }

    @Override
    public void deleteRecording(Recording recording, OperationResult callback) {
        checkNotNull(recording);
        Runnable deleteRunnable = () -> {
            // Delete file from storage.
            File file = new File(recording.getPath());
            boolean deleted = file.delete();
            if (!deleted) {
                appExecutors.mainThread().execute(callback::onFailure);
                return;
            }

            // Delete recording from database.
            int num = recordingsDao.deleteRecording(recording);
            appExecutors.mainThread().execute(() -> {
                if (num > 0)
                    appExecutors.mainThread().execute(callback::onSuccess);
                else
                    appExecutors.mainThread().execute(callback::onFailure);
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
    public void deleteScheduledRecording(ScheduledRecording recording, OperationResult callback) {
        checkNotNull(recording);
        Runnable deleteRunnable = () -> {
            int num = recordingsDao.deleteScheduledRecording(recording);

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
    public void deleteOldScheduledRecordings(long time, OperationResult callback) {
        Runnable deleteRunnable = () -> {
            recordingsDao.deleteOldScheduledRecordings(time);

            appExecutors.mainThread().execute(() -> {
                if (callback == null) return;

                callback.onSuccess();
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
