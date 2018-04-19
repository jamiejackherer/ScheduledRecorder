package com.iclaude.scheduledrecorder.ui.fragments.record;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.RecordingService;
import com.iclaude.scheduledrecorder.SingleLiveEvent;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.iclaude.scheduledrecorder.RecordingService.OnRecordingStatusChangedListener;

/**
 * View model for RecordFragment.
 * Manages the connection with RecordingService and the related data.
 */
public class RecordViewModel extends AndroidViewModel {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";


    public final ObservableBoolean serviceConnected = new ObservableBoolean(false);
    public final ObservableBoolean serviceRecording = new ObservableBoolean(false);
    public final ObservableInt secondsElapsed = new ObservableInt(0);
    private final SingleLiveEvent<Integer> toastMsg = new SingleLiveEvent<>();
    private final MutableLiveData<Integer> amplitudeLive = new MutableLiveData<>();

    private RecordingService recordingService;


    public RecordViewModel(@NonNull Application application) {
        super(application);
    }

    @VisibleForTesting
    public RecordViewModel(Application application, RecordingService recordingService) {
        super(application);
        this.recordingService = recordingService;
    }

    public void connectService(Intent intent) {
        getApplication().startService(intent);
        getApplication().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void disconnectAndStopService(Intent intent) {
        if (!serviceConnected.get()) return;

        getApplication().unbindService(serviceConnection);
        if (!serviceRecording.get())
            getApplication().stopService(intent);
        recordingService.setOnRecordingStatusChangedListener(null);
        recordingService = null;
        serviceConnected.set(false);
    }

    public void startRecording() {
        recordingService.startRecording(0);
        serviceRecording.set(true);
    }

    public void stopRecording() {
        recordingService.stopRecording();
    }

    public SingleLiveEvent<Integer> getToastMsg() {
        return toastMsg;
    }

    public LiveData<Integer> getAmplitudeLive() {
        return amplitudeLive;
    }

    /*
       Implementation of ServiceConnection interface.
       The interaction with the Service is managed by this view model.
   */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            recordingService = ((RecordingService.LocalBinder) iBinder).getService();
            serviceConnected.set(true);
            recordingService.setOnRecordingStatusChangedListener(onScheduledRecordingListener);
            serviceRecording.set(recordingService.isRecording());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (recordingService != null) {
                recordingService.setOnRecordingStatusChangedListener(null);
                recordingService = null;
            }
            serviceConnected.set(false);
        }
    };

    /*
        Implementation of RecordingService.OnRecordingStatusChangedListener interface.
        The Service uses this interface to communicate to the connected component that a
        recording has started/stopped, and the seconds elapsed, so that the UI can be updated
        accordingly.
    */
    private final OnRecordingStatusChangedListener onScheduledRecordingListener =
            new OnRecordingStatusChangedListener() {
        @Override
        public void onRecordingStarted() {
            serviceRecording.set(true);
            toastMsg.postValue(R.string.toast_recording_start);
        }

        @Override
        public void onRecordingStopped(String filePath) {
            serviceRecording.set(false);
            secondsElapsed.set(0);
            toastMsg.postValue(R.string.toast_recording_saved);
        }

        // This method is called from a separate thread.
        @Override
        public void onTimerChanged(int seconds) {
            secondsElapsed.set(seconds);
        }

        @Override
        public void onAmplitudeInfo(int amplitude) {
            amplitudeLive.postValue(amplitude);
        }
    };

    @VisibleForTesting
    public RecordingService getRecordingService() {
        return recordingService;
    }

}
