package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.support.v7.util.DiffUtil;

import com.iclaude.scheduledrecorder.database.Recording;

public class RecordingDiffCallback extends DiffUtil.ItemCallback<Recording> {

    @Override
    public boolean areItemsTheSame(Recording oldItem, Recording newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(Recording oldItem, Recording newItem) {
        return oldItem.getName().equals(newItem.getName()) && oldItem.getLength() == newItem.getLength()
                && oldItem.getTimeAdded() == newItem.getTimeAdded();
    }
}
