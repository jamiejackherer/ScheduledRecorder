/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.SingleLiveEvent;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;


/**
 * ViewModel for FileViewerFragment.
 */

public class FileViewerViewModel extends ViewModel {

    @Inject
    RecordingsRepository recordingsRepository;

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean dataAvailable = new ObservableBoolean(false);

    private final SingleLiveEvent<Recording> playRecordingEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<Recording> longClickItemEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> updateCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deleteCommand = new SingleLiveEvent<>();



    public FileViewerViewModel() {
        App.getComponent().inject(this);
    }

    public LiveData<List<Recording>> getRecordings() {
        dataLoading.set(true);
        LiveData<List<Recording>> recordingsLive = recordingsRepository.getAllRecordings();

        return Transformations.map(recordingsLive, recordings1 -> {
            Collections.reverse(recordings1);
            dataLoading.set(false);
            dataAvailable.set(!recordings1.isEmpty());

            return recordings1;
        });
    }

    public SingleLiveEvent<Recording> getPlayRecordingEvent() {
        return playRecordingEvent;
    }

    public SingleLiveEvent<Recording> getLongClickItemEvent() {
        return longClickItemEvent;
    }

    public SingleLiveEvent<Integer> getUpdateCommand() {
        return updateCommand;
    }

    public void updateRecording(Recording recording, String newName, Context context) {
        // Rename the file.
        String newPath = Utils.getDirectoryPath(context);
        newPath += "/" + newName;
        File f = new File(newPath);
        if (f.exists() && !f.isDirectory()) {
            updateCommand.setValue(R.string.toast_file_exists);
            return;
        }

        File oldFilePath = new File(recording.getPath());
        boolean renamed = oldFilePath.renameTo(f);
        if (!renamed) {
            updateCommand.setValue(R.string.toast_file_rename_error);
            return;
        }

        // Update database.
        recording.setName(newName);
        recording.setPath(newPath);
        recordingsRepository.updateRecordings(new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                updateCommand.setValue(R.string.toast_file_rename);
            }

            @Override
            public void onFailure() {
                updateCommand.setValue(R.string.toast_file_rename_error);
            }
        }, recording);
    }

    public SingleLiveEvent<Integer> getDeleteCommand() {
        return deleteCommand;
    }

    public void deleteRecording(Recording recording) {
        // Delete file from storage.
        File file = new File(recording.getPath());
        boolean deleted = file.delete();
        if (!deleted) {
            deleteCommand.setValue(R.string.toast_file_delete_error);
            return;
        }

        // Delete recording from database.
        recordingsRepository.deleteRecordings(new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                deleteCommand.setValue(R.string.toast_file_delete);
            }

            @Override
            public void onFailure() {
                deleteCommand.setValue(R.string.toast_file_delete_error);
            }
        }, recording);
    }
}
