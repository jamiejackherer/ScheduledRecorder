package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import com.iclaude.scheduledrecorder.database.Recording;

/**
 * Actions that the user can perform on each element of the recordings list.
 */

public interface RecordingItemUserActionListener {
    void onClick(Recording recording);

    boolean onLongClick(Recording recording);
}
