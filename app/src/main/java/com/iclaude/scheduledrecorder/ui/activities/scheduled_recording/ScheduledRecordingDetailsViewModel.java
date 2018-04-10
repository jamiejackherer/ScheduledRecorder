package com.iclaude.scheduledrecorder.ui.activities.scheduled_recording;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.SingleLiveEvent;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.didagger2.App;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.GetScheduledRecordingCallback;
import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE.DATE_START;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT.ERROR;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT.SUCCESS;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.TIME_TYPE.TIME_START;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

/**
 * ViewModel for the Activity displaying a scheduled recording details.
 */

public class ScheduledRecordingDetailsViewModel extends AndroidViewModel implements GetScheduledRecordingCallback {

    public enum OPERATION {
        ADD, EDIT
    }

    public enum DATE_TYPE {
        DATE_START, DATE_END
    }

    public enum TIME_TYPE {
        TIME_START, TIME_END
    }

    public enum RESULT {
        SUCCESS, ERROR
    }


    @Inject
    RecordingsRepository recordingsRepository;

    public final ObservableField<ScheduledRecording> scheduledRecordingObservable = new ObservableField<>();
    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean dataAvailable = new ObservableBoolean(false);
    public final ObservableBoolean timeStartCorrectObservable = new ObservableBoolean();
    public final ObservableBoolean timeEndCorrectObservable = new ObservableBoolean();
    public final ObservableBoolean timesCorrectObservable = new ObservableBoolean();
    public final ObservableField<String> errorMsgObservable = new ObservableField<>("");

    private final SingleLiveEvent<Void> loadedCommand = new SingleLiveEvent<>();
    private final SingleLiveEvent<RESULT> saveCommand = new SingleLiveEvent<>();

    private final Calendar calStart = new GregorianCalendar();
    private final Calendar calEnd = new GregorianCalendar();


    public ScheduledRecordingDetailsViewModel(@NonNull Application application) {
        super(application);
        App.getComponent().inject(this);
    }

    @VisibleForTesting()
    public ScheduledRecordingDetailsViewModel(Application application, RecordingsRepository recordingsRepository) {
        super(application);
        this. recordingsRepository = recordingsRepository;
    }

    public SingleLiveEvent<Void> getLoadedCommand() {
        return loadedCommand;
    }

    // Load scheduled recording from database.
    public void loadScheduledRecordingById(int id) {
        dataLoading.set(true);
        recordingsRepository.getScheduledRecordingById(id, this);
    }

    // Set scheduled recording from outside (i.e. add a new one).
    public void setScheduledRecording(ScheduledRecording scheduledRecording) {
        onSuccess(scheduledRecording);
    }

    // Listeners for getting a scheduled recording from database.
    @Override
    public void onSuccess(ScheduledRecording scheduledRecording) {
        scheduledRecordingObservable.set(scheduledRecording);
        updateRecordingTimes(scheduledRecording);
        dataLoading.set(false);
        dataAvailable.set(true);
        loadedCommand.call();
    }

    @Override
    public void onFailure() {
        scheduledRecordingObservable.set(null);
        dataLoading.set(false);
        dataAvailable.set(false);
    }

    private void updateRecordingTimes(ScheduledRecording rec) {
        if (rec != null) {
            calStart.setTimeInMillis(rec.getStart());
            calEnd.setTimeInMillis(rec.getEnd());
        }

        scheduledRecordingObservable.get().setStart(calStart.getTimeInMillis());
        scheduledRecordingObservable.get().setEnd(calEnd.getTimeInMillis());
        scheduledRecordingObservable.notifyChange();

        timeStartCorrectObservable.set(timeStartFuture());
        timeEndCorrectObservable.set(timeEndFuture());
        timesCorrectObservable.set(timesCorrect());
        if(!timesCorrect()) {
            errorMsgObservable.set(getApplication().getString(R.string.toast_scheduledrecording_timeerror_start_after_end));
        } else if(!timeStartFuture() || !timeEndFuture()) {
            errorMsgObservable.set(getApplication().getString(R.string.toast_scheduledrecording_timeerror_past));
        } else {
            errorMsgObservable.set("");
        }
    }

    public void setDate(DATE_TYPE dateType, int year, int month, int day) {
        Calendar cal = dateType == DATE_START ? calStart : calEnd;
        cal.set(YEAR, year);
        cal.set(MONTH, month);
        cal.set(DAY_OF_MONTH, day);
        updateRecordingTimes(null);
    }

    public void setTime(TIME_TYPE timeType, int hour, int minute) {
        Calendar cal = timeType == TIME_START ? calStart : calEnd;
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(MINUTE, minute);
        updateRecordingTimes(null);
    }

    // Are the times correct?
    private boolean timesCorrect() {
        return !(calEnd.before(calStart) || calEnd.equals(calStart));
    }

    // Is the starting time in the future?
    private boolean timeStartFuture() {
        return calStart.getTimeInMillis() > System.currentTimeMillis();
    }

    // Is the ending time in the future?
    private boolean timeEndFuture() {
        return calEnd.getTimeInMillis() > System.currentTimeMillis();
    }

    // The user clicks the save button in the action bar.
    public SingleLiveEvent<RESULT> getSaveTaskCommand() {
        return saveCommand;
    }

    public void saveScheduledRecording(OPERATION operation) {
        // Check that the times are correct.
        if (!timeStartFuture() || !timeEndFuture()) {
            return;
        }
        if (!timesCorrect()) {
            return;
        }

        // Try updating or inserting the recording.
        if (operation == OPERATION.EDIT)
            updateScheduledRecording();
        else
            insertScheduledRecording();
    }

    private void updateScheduledRecording() {
        ScheduledRecording scheduledRecording = scheduledRecordingObservable.get();
        recordingsRepository.getNumRecordingsAlreadyScheduled(scheduledRecording.getStart(), scheduledRecording.getEnd(), scheduledRecording.getId(), count -> {
            if(count > 0) {
                saveCommand.setValue(ERROR);
            } else {
                recordingsRepository.updateScheduledRecordings(new OperationResult() {
                    @Override
                    public void onSuccess() {
                        saveCommand.setValue(SUCCESS);
                    }

                    @Override
                    public void onFailure() {
                        saveCommand.setValue(ERROR);
                    }
                }, scheduledRecording);
            }
        });
    }

    private void insertScheduledRecording() {
        ScheduledRecording scheduledRecording = scheduledRecordingObservable.get();
        recordingsRepository.getNumRecordingsAlreadyScheduled(scheduledRecording.getStart(), scheduledRecording.getEnd(), scheduledRecording.getId(), count -> {
            if (count > 0) {
                saveCommand.setValue(ERROR);
            } else {
                recordingsRepository.insertScheduledRecording(scheduledRecordingObservable.get(), new OperationResult() {
                    @Override
                    public void onSuccess() {
                        saveCommand.setValue(SUCCESS);
                    }

                    @Override
                    public void onFailure() {
                        saveCommand.setValue(ERROR);
                    }
                });
            }
        });
    }
}
