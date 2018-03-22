/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.activities.scheduled_recording;


import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT;

/**
 * User actions in ScheduledRecordingActivity.
 */

public interface ScheduledRecordingDetailsNavigator {
    void onScheduledRecordingLoaded();

    void onScheduledRecordingSaved(RESULT result);
}
