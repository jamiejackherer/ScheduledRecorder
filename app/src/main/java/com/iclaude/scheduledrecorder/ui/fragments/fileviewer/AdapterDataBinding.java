/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerItemBinding;

import java.util.List;


/**
 * Adapter for RecyclerViews using data binding.
 */

public class AdapterDataBinding extends RecyclerView.Adapter<ViewHolderDataBinding> {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();

    private final Context context;
    private List<Recording> recordings;
    private final RecordingsClickCallback clickCallback;


    public AdapterDataBinding(Context context, List<Recording> recordings, RecordingsClickCallback clickCallback) {
        this.context = context;
        this.recordings = recordings;
        this.clickCallback = clickCallback;
    }

    public ViewHolderDataBinding onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentFileViewerItemBinding binding = FragmentFileViewerItemBinding.inflate(layoutInflater, parent, false);
        binding.setClickCallback(clickCallback);

        return new ViewHolderDataBinding(binding);
    }

    public void onBindViewHolder(ViewHolderDataBinding holder, int position) {
        Recording recording = recordings.get(position);
        holder.bind(recording);
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
