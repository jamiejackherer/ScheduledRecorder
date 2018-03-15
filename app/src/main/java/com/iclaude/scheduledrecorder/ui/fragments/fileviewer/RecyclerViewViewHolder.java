/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerItemBinding;

/**
 * ViewHolder for using data binding in RecyclerViews.
 */

public class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private final FragmentFileViewerItemBinding binding;
    private final Context context;

    public RecyclerViewViewHolder(FragmentFileViewerItemBinding binding, Context context) {
        super(binding.getRoot());
        this.binding = binding;

        this.context = context;
    }

    public void bind(RecordingViewModel viewModel) {
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

}

