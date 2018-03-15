/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerItemBinding;

import java.util.List;


/**
 * Adapter for RecyclerViews using data binding.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewViewHolder> {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();

    private List<Recording> recordings;


    public RecyclerViewAdapter(List<Recording> recordings) {
        this.recordings = recordings;
    }

    public RecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentFileViewerItemBinding binding = FragmentFileViewerItemBinding.inflate(layoutInflater, parent, false);

        return new RecyclerViewViewHolder(binding, parent.getContext());
    }

    public void onBindViewHolder(RecyclerViewViewHolder holder, int position) {
        //RecordingViewModel recordingViewModel = ViewModelProviders.of((FragmentActivity) holder.getContext()).get(RecordingViewModel.class);
        //recordingViewModel.setRecording(recordings.get(position));
        RecordingViewModel recordingViewModel = new RecordingViewModel(recordings.get(position));
        holder.bind(recordingViewModel);
    }

    public void setRecordings(List<Recording> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }
}
