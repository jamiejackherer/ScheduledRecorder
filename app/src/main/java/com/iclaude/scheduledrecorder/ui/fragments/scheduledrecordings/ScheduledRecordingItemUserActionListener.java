package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import com.iclaude.scheduledrecorder.database.ScheduledRecording;

/**
 * Actions that the user can perform on each element of the scheduled recordings list.
 */
public interface ScheduledRecordingItemUserActionListener {
    void onClick(ScheduledRecording scheduledRecording);

    @SuppressWarnings("SameReturnValue")
    boolean onLongClick(ScheduledRecording scheduledRecording);
}
