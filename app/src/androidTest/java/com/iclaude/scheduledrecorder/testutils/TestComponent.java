package com.iclaude.scheduledrecorder.testutils;

import com.iclaude.scheduledrecorder.RecordingServiceTest;
import com.iclaude.scheduledrecorder.espresso.EspressoRecordFragment;
import com.iclaude.scheduledrecorder.espresso.EspressoScheduledRecordingDetailsActivity;
import com.iclaude.scheduledrecorder.espresso.EspressoScheduledRecordingsFragment;
import com.iclaude.scheduledrecorder.didagger2.AppComponent;
import com.iclaude.scheduledrecorder.didagger2.AppModule;
import com.iclaude.scheduledrecorder.didagger2.DatabaseModule;

import javax.inject.Singleton;

import dagger.Component;

/*
    Component necessary to inject our RecordingsRepository into Espresso test classes.
 */
@Singleton
@Component(modules = {AppModule.class, DatabaseModule.class})
public interface TestComponent extends AppComponent {
    void inject(EspressoRecordFragment fragment);

    void inject(EspressoScheduledRecordingDetailsActivity activity);

    void inject(EspressoScheduledRecordingsFragment fragment);

    void inject(RecordingServiceTest recordingServiceTest);
}
