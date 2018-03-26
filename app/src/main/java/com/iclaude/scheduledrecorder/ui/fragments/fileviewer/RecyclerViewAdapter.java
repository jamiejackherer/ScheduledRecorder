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

    private List<Recording> recordings;
    private final FileViewerViewModel fileViewerViewModel;


    public RecyclerViewAdapter(List<Recording> recordings, FileViewerViewModel fileViewerViewModel) {
        this.recordings = recordings;
        this.fileViewerViewModel = fileViewerViewModel;
    }

    public RecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentFileViewerItemBinding binding = FragmentFileViewerItemBinding.inflate(layoutInflater, parent, false);

        RecordingItemUserActionListener listener = new RecordingItemUserActionListener() {
            @Override
            public void onClick(Recording recording) {
                fileViewerViewModel.getPlayRecordingEvent().setValue(recording);
            }

            @Override
            public boolean onLongClick(Recording recording) {
                fileViewerViewModel.getLongClickItemEvent().setValue(recording);
                return true;
            }
        };
        binding.setListener(listener);

        return new RecyclerViewViewHolder(binding);
    }

    public void onBindViewHolder(RecyclerViewViewHolder holder, int position) {
        holder.bind(recordings.get(position));
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
