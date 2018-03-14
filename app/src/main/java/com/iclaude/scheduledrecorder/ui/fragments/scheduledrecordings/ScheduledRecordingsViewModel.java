/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;

import java.util.List;

import javax.inject.Inject;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;

/**
 * ViewModel for ScheduledRecordingsFragment.
 */

public class ScheduledRecordingsViewModel extends ViewModel {
    @Inject
    RecordingsRepository recordingsRepository;
    private LiveData<List<ScheduledRecording>> recordings;

    public ScheduledRecordingsViewModel() {
        App.getComponent().inject(this);
    }

    public LiveData<List<ScheduledRecording>> getScheduledRecordings() {
        return recordingsRepository.getAllScheduledRecordings();
    }

    public void deleteScheduledRecording(ScheduledRecording scheduledRecording, OperationResult callback) {
        recordingsRepository.deleteScheduledRecordings(callback, scheduledRecording);
    }
}
