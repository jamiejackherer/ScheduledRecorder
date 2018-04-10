/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.didagger2;

import com.iclaude.scheduledrecorder.RecordingService;
import com.iclaude.scheduledrecorder.ScheduledRecordingService;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel;
import com.iclaude.scheduledrecorder.ui.fragments.fileviewer.FileViewerViewModel;
import com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings.ScheduledRecordingsFragment;
import com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings.ScheduledRecordingsViewModel;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Dagger @Component class.
 */
@Component(modules = {AppModule.class, DatabaseModule.class})
@Singleton
public interface AppComponent {

    // App.
    void inject(RecordingService recordingService);

    void inject(ScheduledRecordingService scheduledRecordingService);

    void inject(RecordingsRepository recordingsRepository);

    void inject(FileViewerViewModel fileViewerViewModel);

    void inject(ScheduledRecordingsViewModel scheduledRecordingsViewModel);

    void inject(ScheduledRecordingsFragment scheduledRecordingsFragment);

    void inject(ScheduledRecordingDetailsViewModel scheduledRecordingDetailsViewModel);
}
