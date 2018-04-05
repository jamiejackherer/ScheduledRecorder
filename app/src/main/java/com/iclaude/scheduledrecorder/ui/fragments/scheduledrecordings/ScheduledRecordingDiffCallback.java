package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.support.v7.util.DiffUtil;

import com.iclaude.scheduledrecorder.database.ScheduledRecording;

class ScheduledRecordingDiffCallback extends DiffUtil.ItemCallback<ScheduledRecording> {
    @Override
    public boolean areItemsTheSame(ScheduledRecording oldItem, ScheduledRecording newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(ScheduledRecording oldItem, ScheduledRecording newItem) {
        return oldItem.getStart() == newItem.getStart() && oldItem.getEnd() == newItem.getEnd();
    }
}
