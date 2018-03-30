package com.iclaude.scheduledrecorder.ui.fragments.record;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.RecordingService;
import com.iclaude.scheduledrecorder.SingleLiveEvent;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * View model for RecordFragment.
 * Manages the connection with RecordingService and the related data.
 */
public class RecordViewModel extends AndroidViewModel {
    public final ObservableBoolean serviceConnected = new ObservableBoolean(false);
    public final ObservableBoolean serviceRecording = new ObservableBoolean(false);
    public final ObservableInt secondsElapsed = new ObservableInt(0);
    private final SingleLiveEvent<String> toastMsg = new SingleLiveEvent<>();

    private RecordingService recordingService;
    private Activity activity;


    public RecordViewModel(@NonNull Application application) {
        super(application);
    }

    public void connectService(Activity activity) {
        this.activity = activity;

        getApplication().startService(RecordingService.makeIntent(getApplication(), true));
        getApplication().bindService(RecordingService.makeIntent(getApplication(), true), serviceConnection, BIND_AUTO_CREATE);
    }

    public void disconnectService() {
        if (!serviceConnected.get()) return;

        getApplication().unbindService(serviceConnection);
        if (!serviceRecording.get())
            getApplication().stopService(RecordingService.makeIntent(getApplication()));
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

    public SingleLiveEvent<String> getToastMsg() {
        return toastMsg;
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
            recordingService.setOnRecordingStatusChangedListener(null);
            recordingService = null;
            serviceConnected.set(false);
        }
    };

    /*
        Implementation of RecordingService.OnRecordingStatusChangedListener interface.
        The Service uses this interface to communicate to the connected component that a
        recording has started/stopped, and the seconds elapsed, so that the UI can be updated
        accordingly.
    */
    private final RecordingService.OnRecordingStatusChangedListener onScheduledRecordingListener =
            new RecordingService.OnRecordingStatusChangedListener() {
        @Override
        public void onRecordingStarted() {
            serviceRecording.set(true);
            toastMsg.setValue(getApplication().getString(R.string.toast_recording_start));
        }

        @Override
        public void onRecordingStopped(String filePath) {
            serviceRecording.set(false);
            secondsElapsed.set(0);
            toastMsg.setValue(getApplication().getString(R.string.toast_recording_finish) + " " + filePath);
        }

        // This method is called from a separate thread.
        @Override
        public void onTimerChanged(int seconds) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    secondsElapsed.set(seconds);
                }
            });
        }
    };


}
