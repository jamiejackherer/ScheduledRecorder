/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.support.v7.widget.RecyclerView;

import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerItemBinding;

/**
 * ViewHolder for using data binding in RecyclerViews.
 */

public class RecyclerViewViewHolder extends RecyclerView.ViewHolder {

    private final FragmentFileViewerItemBinding binding;

    public RecyclerViewViewHolder(FragmentFileViewerItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

    }

    public void bind(Recording recording) {
        binding.setRecording(recording);
        binding.executePendingBindings();
    }

}

