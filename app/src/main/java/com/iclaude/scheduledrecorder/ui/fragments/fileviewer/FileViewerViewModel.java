/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.ObservableBoolean;
import android.support.annotation.VisibleForTesting;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.SingleLiveEvent;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.didagger2.App;

import java.util.List;

import javax.inject.Inject;


/**
 * ViewModel for FileViewerFragment.
 */

public class FileViewerViewModel extends AndroidViewModel {

    @Inject
    RecordingsRepository recordingsRepository;

    public final ObservableBoolean dataLoading = new ObservableBoolean(false);
    public final ObservableBoolean dataAvailable = new ObservableBoolean(false);

    private final SingleLiveEvent<Recording> playRecordingEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<Recording> longClickItemEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> updateCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deleteCommand = new SingleLiveEvent<>();


    public FileViewerViewModel(Application application) {
        super(application);
        App.getComponent().inject(this);
    }

    @VisibleForTesting()
    public FileViewerViewModel(Application application, RecordingsRepository recordingsRepository) {
        super(application);
        this.recordingsRepository = recordingsRepository;
    }

    public LiveData<List<Recording>> getRecordings() {
        dataLoading.set(true);
        LiveData<List<Recording>> recordingsLive = recordingsRepository.getAllRecordings();

        LiveData<List<Recording>> recordingsLive2 = Transformations.switchMap(recordingsLive, recordings -> {
            dataLoading.set(false);
            dataAvailable.set(!recordings.isEmpty());

            MutableLiveData<List<Recording>> result = new MutableLiveData<>();
            result.setValue(recordings);
            return result;
        });

        return recordingsLive2;
    }

    public SingleLiveEvent<Recording> getPlayRecordingEvent() {
        return playRecordingEvent;
    }

    public void playRecording(Recording recording) {
        playRecordingEvent.setValue(recording);
    }

    public SingleLiveEvent<Recording> getLongClickItemEvent() {
        return longClickItemEvent;
    }

    public void showLongClickDialogOptions(Recording recording) {
        longClickItemEvent.setValue(recording);
    }

    public SingleLiveEvent<Integer> getUpdateCommand() {
        return updateCommand;
    }

    public void updateRecording(Recording recording, String newName) {
        recordingsRepository.updateRecording(recording, newName, getApplication(), new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                updateCommand.setValue(R.string.toast_file_renamed);
            }

            @Override
            public void onFailure() {
                updateCommand.setValue(R.string.toast_file_renamed_error);
            }
        });
    }

    public SingleLiveEvent<Integer> getDeleteCommand() {
        return deleteCommand;
    }

    public void deleteRecording(Recording recording) {
        recordingsRepository.deleteRecording(recording, new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                deleteCommand.setValue(R.string.toast_recording_deleted);
            }

            @Override
            public void onFailure() {
                deleteCommand.setValue(R.string.toast_recording_deleted_error);
            }
        });
    }
}
