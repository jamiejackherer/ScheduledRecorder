/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.SingleLiveEvent;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

/**
 * ViewModel for ScheduledRecordingsFragment.
 */

public class ScheduledRecordingsViewModel extends ViewModel {

    @Inject
    RecordingsRepository recordingsRepository;

    // Observables.
    private LiveData<List<ScheduledRecording>> listLive;
    private final MutableLiveData<List<ScheduledRecording>> listFilteredLive = new MutableLiveData<>();
    public final ObservableBoolean dataLoading = new ObservableBoolean(false);
    public final ObservableBoolean dataAvailable = new ObservableBoolean(false);
    public final ObservableField<Date> selectedDate = new ObservableField<>(new Date(System.currentTimeMillis()));
    public final ObservableField<Date> selectedMonth = new ObservableField<>(new Date(System.currentTimeMillis()));

    // Commands.
    private final SingleLiveEvent<Void> addCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<ScheduledRecording> editCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<ScheduledRecording> longClickItemEvent = new SingleLiveEvent<>();
    private final SingleLiveEvent<Integer> deleteCommand = new SingleLiveEvent<>();


    public ScheduledRecordingsViewModel() {
        App.getComponent().inject(this);
    }

    // Observables.
    public LiveData<List<ScheduledRecording>> getScheduledRecordings() {
        if(listLive != null)
            return listLive;

        dataLoading.set(true);
        listLive = recordingsRepository.getAllScheduledRecordings();
        dataLoading.set(false);
        filterList();

        return listLive;
    }

    public LiveData<List<ScheduledRecording>> getScheduledRecordingsFiltered() {
        return listFilteredLive;
    }

    private void filterList() {
        if(listLive.getValue() == null)
            return;

        long filterStart = Utils.getDayStartTimeLong(Objects.requireNonNull(selectedDate.get()));
        long filterEnd = Utils.getDayEndTimeLong(Objects.requireNonNull(selectedDate.get()));

        List<ScheduledRecording> scheduledRecordingsFiltered = new ArrayList<>();
        for(ScheduledRecording rec : listLive.getValue()) {
            if((rec.getStart() >= filterStart && rec.getStart() < filterEnd) || (rec.getEnd() >= filterStart && rec.getEnd() < filterEnd))
                scheduledRecordingsFiltered.add(rec);
        }

        Collections.sort(scheduledRecordingsFiltered);
        listFilteredLive.setValue(scheduledRecordingsFiltered);
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate.set(selectedDate);
        filterList();
    }

    // Commands.
    public SingleLiveEvent<Void> getAddCommand() {
        return addCommand;
    }

    public void addScheduledRecording() {
        addCommand.call();
    }

    public SingleLiveEvent<ScheduledRecording> getEditCommand() {
        return editCommand;
    }

    public void editScheduledRecording(ScheduledRecording scheduledRecording) {
        editCommand.setValue(scheduledRecording);
    }

    public SingleLiveEvent<ScheduledRecording> getLongClickItemEvent() {
        return longClickItemEvent;
    }

    public void showLongClickDialogOptions(ScheduledRecording scheduledRecording) {
        longClickItemEvent.setValue(scheduledRecording);
    }

    public SingleLiveEvent<Integer> getDeleteCommand() {
        return deleteCommand;
    }

    public void deleteScheduledRecording(ScheduledRecording scheduledRecording) {
        recordingsRepository.deleteScheduledRecording(scheduledRecording, new RecordingsRepositoryInterface.OperationResult() {
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
