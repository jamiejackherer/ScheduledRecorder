package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import com.iclaude.scheduledrecorder.database.Recording;

/**
 * Actions the user can perform on a recording.
 */

public interface RecordingUserActions {
    void playRecording(Recording recording);

    void shareFile(Recording recording);

    void renameFile(Recording recording);

    void deleteFile(Recording recording);
}
