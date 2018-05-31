package com.iclaude.scheduledrecorder.record;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.RecordingService;
import com.iclaude.scheduledrecorder.testutils.TestUtils;
import com.iclaude.scheduledrecorder.ui.fragments.record.RecordViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static android.content.Context.BIND_AUTO_CREATE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of RecordViewModel.
 */
public class RecordViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private RecordingService recordingService;
    @Mock
    private Application context;
    @Mock
    private RecordingService.LocalBinder iBinder;
    @Mock
    private ComponentName componentName;
    @Captor
    private ArgumentCaptor<ServiceConnection> serviceConnectionArgumentCaptor;
    @Captor
    private ArgumentCaptor<RecordingService.OnRecordingStatusChangedListener> onRecordingStatusChangedListenerArgumentCaptor;

    private RecordViewModel recordViewModel;
    private Intent intent;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        recordViewModel = new RecordViewModel(context, recordingService);

        intent = new Intent(context, RecordingService.class);
        intent.putExtra("com.danielkim.soundrecorder.EXTRA_ACTIVITY_STARTER", true);

        when(iBinder.getService()).thenReturn(recordingService);
        when(context.getString(R.string.toast_recording_start)).thenReturn("Recording started");
        when(context.getString(R.string.toast_recording_saved)).thenReturn("Recording saved to");
    }

    @Test
    public void testInitialValues() {
        assertFalse(recordViewModel.serviceConnected.get());
        assertFalse(recordViewModel.serviceRecording.get());
        assertEquals(0, recordViewModel.secondsElapsed.get());
    }

    @Test
    public void testStartRecording() {
        recordViewModel.startRecording();
        verify(recordingService).startRecording(0);
        assertTrue(recordViewModel.serviceRecording.get());
    }

    @Test
    public void testStopRecording() {
        recordViewModel.stopRecording();
        verify(recordingService).stopRecording();
    }

    @Test
    public void testServiceConnectionAndStop() {

        // Connection to service.
        recordViewModel.connectService(intent);
        verify(context).bindService(eq(intent), serviceConnectionArgumentCaptor.capture(), eq(BIND_AUTO_CREATE));
        serviceConnectionArgumentCaptor.getValue().onServiceConnected(componentName, iBinder);
        assertTrue(recordViewModel.serviceConnected.get());
        assertFalse(recordViewModel.serviceRecording.get());
        verify(recordingService).setOnRecordingStatusChangedListener(onRecordingStatusChangedListenerArgumentCaptor.capture());

        // Start recording.
        Observer<Integer> observer = mock(Observer.class);
        recordViewModel.getToastMsg().observe(TestUtils.TEST_OBSERVER, observer);
        recordViewModel.startRecording();
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onRecordingStarted();
        assertTrue(recordViewModel.serviceRecording.get());
        verify(observer).onChanged(R.string.toast_recording_start);

        // Change seconds elapsed.
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onTimerChanged(10);
        assertEquals(10, recordViewModel.secondsElapsed.get());

        // Stop recording.
        recordViewModel.stopRecording();
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onRecordingStopped("file_path");
        assertFalse(recordViewModel.serviceRecording.get());
        assertEquals(0, recordViewModel.secondsElapsed.get());
        verify(observer).onChanged(R.string.toast_recording_saved);

        // Disconnect and stop Service.
        Intent stopIntent = new Intent(context, RecordingService.class);
        recordViewModel.disconnectAndStopService(stopIntent);
        verify(context).unbindService(any(ServiceConnection.class));
        verify(context).stopService(stopIntent);
        verify(recordingService).setOnRecordingStatusChangedListener(null);
        assertNull(recordViewModel.getRecordingService());
        assertFalse(recordViewModel.serviceConnected.get());
    }

    @Test
    public void testServiceConnectionAndDisconnection() {

        // Connection to service.
        recordViewModel.connectService(intent);
        verify(context).bindService(eq(intent), serviceConnectionArgumentCaptor.capture(), eq(BIND_AUTO_CREATE));
        serviceConnectionArgumentCaptor.getValue().onServiceConnected(componentName, iBinder);
        assertTrue(recordViewModel.serviceConnected.get());
        assertFalse(recordViewModel.serviceRecording.get());
        verify(recordingService).setOnRecordingStatusChangedListener(onRecordingStatusChangedListenerArgumentCaptor.capture());

        // Start recording.
        Observer<Integer> observer = mock(Observer.class);
        recordViewModel.getToastMsg().observe(TestUtils.TEST_OBSERVER, observer);
        recordViewModel.startRecording();
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onRecordingStarted();
        assertTrue(recordViewModel.serviceRecording.get());
        verify(observer).onChanged(R.string.toast_recording_start);

        // Change seconds elapsed.
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onTimerChanged(10);
        assertEquals(10, recordViewModel.secondsElapsed.get());

        // Stop recording.
        recordViewModel.stopRecording();
        onRecordingStatusChangedListenerArgumentCaptor.getValue().onRecordingStopped("file_path");
        assertFalse(recordViewModel.serviceRecording.get());
        assertEquals(0, recordViewModel.secondsElapsed.get());
        verify(observer).onChanged(R.string.toast_recording_saved);

        // Disconnect service automatically
        serviceConnectionArgumentCaptor.getValue().onServiceDisconnected(componentName);
        verify(recordingService).setOnRecordingStatusChangedListener(null);
        assertNull(recordViewModel.getRecordingService());
        assertFalse(recordViewModel.serviceConnected.get());
    }
}
