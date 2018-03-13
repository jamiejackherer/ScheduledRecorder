/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.fragments.fileviewer;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.iclaude.scheduledrecorder.BR;

/**
 * Generic ViewHolder for using data binding in RecyclerViews.
 */

public class RVViewHolderDataBinding extends RecyclerView.ViewHolder {

    private final ViewDataBinding binding;

    public RVViewHolderDataBinding(ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Object obj) {
        binding.setVariable(BR.obj, obj);
        binding.executePendingBindings();
    }

    public ViewDataBinding getBinding() {
        return binding;
    }
}

