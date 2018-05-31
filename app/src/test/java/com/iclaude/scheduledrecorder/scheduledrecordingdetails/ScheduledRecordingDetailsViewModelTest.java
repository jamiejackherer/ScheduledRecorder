package com.iclaude.scheduledrecorder.scheduledrecordingdetails;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.GetRecordingsCountCallback;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE.DATE_END;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE.DATE_START;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.OPERATION.ADD;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.OPERATION.EDIT;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT.ERROR;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT.SUCCESS;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.TIME_TYPE.TIME_END;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.TIME_TYPE.TIME_START;
import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of ScheduledRecordingDetailsViewModel.
 */

public class ScheduledRecordingDetailsViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    // Scheduled recording data used in test.
    private static final int ID = 69;
    private static final long MINUTE = 1000 * 60;
    private static final long START = System.currentTimeMillis() + MINUTE * 60;
    private static final long END = START + MINUTE * 30;
    private static final long END_ERROR = START - MINUTE * 30;
    private static final long START_PAST = System.currentTimeMillis() - MINUTE * 60;
    private static final long END_PAST = START_PAST + MINUTE * 30;
    private static final String ERROR_MSG_PAST = "Recording scheduled in the past";
    private static final String ERROR_MSG_TIMES = "Start time after end time";

    // ViewModel and its scheduled recording.
    private ScheduledRecordingDetailsViewModel viewModel;
    private ScheduledRecording scheduledRecording;
    private ScheduledRecording scheduledRecordingPast;
    private ScheduledRecording scheduledRecordingError;

    @Mock
    private Application mContext;
    @Mock
    private RecordingsRepository recordingsRepository;
    @Mock
    private GetRecordingsCountCallback getRecordingsCountCallback;
    @Mock
    private OperationResult operationResult;
    @Captor
    private ArgumentCaptor<RecordingsRepositoryInterface.GetScheduledRecordingCallback> getScheduledRecordingCallbackCaptor;
    @Captor
    private ArgumentCaptor<GetRecordingsCountCallback> getRecordingsCountCallbackCaptor;
    @Captor
    private ArgumentCaptor<RecordingsRepositoryInterface.OperationResult> operationResultCaptor;


    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Create the class under test and the object that it manages.
        viewModel = new ScheduledRecordingDetailsViewModel(mContext, recordingsRepository);
        scheduledRecording = new ScheduledRecording(ID, START, END);
        scheduledRecordingPast = new ScheduledRecording(ID, START_PAST, END_PAST);
        scheduledRecordingError = new ScheduledRecording(ID, START, END_ERROR);

        // Setup context.
        when(mContext.getString(R.string.toast_scheduledrecording_timeerror_start_after_end)).thenReturn(ERROR_MSG_TIMES);
        when(mContext.getString(R.string.toast_scheduledrecording_timeerror_past)).thenReturn(ERROR_MSG_PAST);
    }

    // Tests.
    @Test
    public void testLoadRecording() {
        loadRecording(scheduledRecording);

        // Then verify that the view was notified.
        assertEquals(viewModel.scheduledRecordingObservable.get().getStart(), scheduledRecording.getStart());
        assertEquals(viewModel.scheduledRecordingObservable.get().getEnd(), scheduledRecording.getEnd());
        assertEquals(viewModel.dataLoading.get(), false);
        assertEquals(viewModel.dataAvailable.get(), true);
    }

    @Test
    public void testSetRecording() {
        setRecording();

        // Then verify that the view was notified
        assertEquals(viewModel.scheduledRecordingObservable.get().getStart(), scheduledRecording.getStart());
        assertEquals(viewModel.scheduledRecordingObservable.get().getEnd(), scheduledRecording.getEnd());
        assertEquals(viewModel.dataLoading.get(), false);
        assertEquals(viewModel.dataAvailable.get(), true);
    }

    @Test
    public void testLoadRecordingError() {
        loadRecordingError();

        assertEquals(viewModel.scheduledRecordingObservable.get(), null);
        assertEquals(viewModel.dataLoading.get(), false);
        assertEquals(viewModel.dataAvailable.get(), false);
    }

    @Test
    public void testLoadRecordingInfinite() {
        loadRecordingInfinite();

        assertEquals(viewModel.scheduledRecordingObservable.get(), null);
        assertEquals(viewModel.dataLoading.get(), true);
        assertEquals(viewModel.dataAvailable.get(), false);
    }

    @Test
    public void testEditAddRecordingPast() {
        loadRecording(scheduledRecordingPast);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_PAST);

        viewModel.saveScheduledRecording(EDIT);
        assertEquals(viewModel.timeStartCorrectObservable.get(), false);
        assertEquals(viewModel.timeEndCorrectObservable.get(), false);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository, never()).getNumRecordingsAlreadyScheduled(eq(scheduledRecordingPast.getStart()), eq(scheduledRecordingPast.getEnd()), eq(scheduledRecordingPast.getId()), eq(getRecordingsCountCallback));
        verify(recordingsRepository, never()).updateScheduledRecordings(eq(operationResult), eq(scheduledRecordingPast));
        assertEquals(viewModel.getSaveTaskCommand().getValue(), null);
    }

    @Test
    public void testEditAddRecordingStartAfterEnd() {
        loadRecording(scheduledRecordingError);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_TIMES);

        viewModel.saveScheduledRecording(EDIT);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), false);

        verify(recordingsRepository, never()).getNumRecordingsAlreadyScheduled(eq(scheduledRecordingPast.getStart()), eq(scheduledRecordingPast.getEnd()), eq(scheduledRecordingPast.getId()), eq(getRecordingsCountCallback));
        verify(recordingsRepository, never()).updateScheduledRecordings(eq(operationResult), eq(scheduledRecordingPast));
        assertEquals(viewModel.getSaveTaskCommand().getValue(), null);
    }

    @Test
    public void testEditRecordingCorrect() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(EDIT);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(0);
        verify(recordingsRepository).updateScheduledRecordings(operationResultCaptor.capture(), eq(scheduledRecording));
        operationResultCaptor.getValue().onSuccess();

        assertEquals(viewModel.getSaveTaskCommand().getValue(), SUCCESS);
    }

    @Test
    public void testEditRecordingFailureAlreadyScheduled() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(EDIT);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(1);
        verify(recordingsRepository, never()).updateScheduledRecordings(eq(operationResult), eq(scheduledRecording));

        assertEquals(viewModel.getSaveTaskCommand().getValue(), ERROR);
    }

    @Test
    public void testEditRecordingFailureGeneric() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(EDIT);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(0);
        verify(recordingsRepository).updateScheduledRecordings(operationResultCaptor.capture(), eq(scheduledRecording));
        operationResultCaptor.getValue().onFailure();

        assertEquals(viewModel.getSaveTaskCommand().getValue(), ERROR);
    }

    @Test
    public void testAddRecordingCorrect() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(ADD);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(0);
        verify(recordingsRepository).insertScheduledRecording(eq(scheduledRecording), operationResultCaptor.capture());
        operationResultCaptor.getValue().onSuccess();

        assertEquals(viewModel.getSaveTaskCommand().getValue(), SUCCESS);
    }

    @Test
    public void testAddRecordingFailureAlreadyScheduled() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(ADD);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(1);
        verify(recordingsRepository, never()).insertScheduledRecording(eq(scheduledRecording), eq(operationResult));

        assertEquals(viewModel.getSaveTaskCommand().getValue(), ERROR);
    }

    @Test
    public void testAddRecordingFailureGeneric() {
        loadRecording(scheduledRecording);
        assertEquals(viewModel.errorMsgObservable.get(), "");

        viewModel.saveScheduledRecording(ADD);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);

        verify(recordingsRepository).getNumRecordingsAlreadyScheduled(eq(scheduledRecording.getStart()), eq(scheduledRecording.getEnd()), eq(scheduledRecording.getId()), getRecordingsCountCallbackCaptor.capture());
        getRecordingsCountCallbackCaptor.getValue().recordingsCount(0);
        verify(recordingsRepository).insertScheduledRecording(eq(scheduledRecording), operationResultCaptor.capture());
        operationResultCaptor.getValue().onFailure();

        assertEquals(viewModel.getSaveTaskCommand().getValue(), ERROR);
    }

    @Test
    public void testSetDate() {
        loadRecording(scheduledRecording);

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(scheduledRecording.getStart());
        cal.roll(Calendar.DAY_OF_MONTH, -1);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        viewModel.setDate(DATE_END, year, month, day);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), false);
        assertEquals(viewModel.timesCorrectObservable.get(), false);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_TIMES);

        viewModel.setDate(DATE_START, year, month, day-1);
        assertEquals(viewModel.timeStartCorrectObservable.get(), false);
        assertEquals(viewModel.timeEndCorrectObservable.get(), false);
        assertEquals(viewModel.timesCorrectObservable.get(), true);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_PAST);

        viewModel.setDate(DATE_START, year, month, day);
        viewModel.setDate(DATE_END, year, month, day+2);

        assertEquals(viewModel.timeStartCorrectObservable.get(), false);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_PAST);
    }

    @Test
    public void testSetTime() {
        loadRecording(scheduledRecording);

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(scheduledRecording.getStart());
        cal.add(Calendar.HOUR_OF_DAY, -2);
        cal.add(Calendar.HOUR_OF_DAY, -2);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        viewModel.setTime(TIME_END, hour, minute);
        assertEquals(viewModel.timeStartCorrectObservable.get(), true);
        assertEquals(viewModel.timeEndCorrectObservable.get(), false);
        assertEquals(viewModel.timesCorrectObservable.get(), false);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_TIMES);

        viewModel.setTime(TIME_START, hour-1, minute);
        assertEquals(viewModel.timeStartCorrectObservable.get(), false);
        assertEquals(viewModel.timeEndCorrectObservable.get(), false);
        assertEquals(viewModel.timesCorrectObservable.get(), true);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_PAST);

        viewModel.setTime(TIME_START, hour, minute);
        viewModel.setTime(TIME_END, hour+4, minute);

        assertEquals(viewModel.timeStartCorrectObservable.get(), false);
        assertEquals(viewModel.timeEndCorrectObservable.get(), true);
        assertEquals(viewModel.timesCorrectObservable.get(), true);
        assertEquals(viewModel.errorMsgObservable.get(), ERROR_MSG_PAST);

    }
    // Utility methods.
    private void loadRecording(ScheduledRecording mRecording) {
        // Given an initialized ViewModel with an active recording loaded from repository.
        viewModel.loadScheduledRecordingById(mRecording.getId());

        // Use a captor to get a reference for the getScheduledRecordingCallback.
        verify(recordingsRepository).getScheduledRecordingById(eq(mRecording.getId()), getScheduledRecordingCallbackCaptor.capture());

        // Trigger getScheduledRecordingCallback.
        getScheduledRecordingCallbackCaptor.getValue().onSuccess(mRecording);
    }

    private void loadRecordingError() {
        // Given an initialized ViewModel with an active recording loaded from repository.
        viewModel.loadScheduledRecordingById(scheduledRecording.getId());

        // Use a captor to get a reference for the getScheduledRecordingCallback.
        verify(recordingsRepository).getScheduledRecordingById(eq(scheduledRecording.getId()), getScheduledRecordingCallbackCaptor.capture());

        // Trigger getScheduledRecordingCallback.
        getScheduledRecordingCallbackCaptor.getValue().onFailure();
    }

    private void loadRecordingInfinite() {
        // Given an initialized ViewModel with an active recording loaded from repository.
        viewModel.loadScheduledRecordingById(scheduledRecording.getId());

        // Use a captor to get a reference for the getScheduledRecordingCallback.
        verify(recordingsRepository).getScheduledRecordingById(eq(scheduledRecording.getId()), getScheduledRecordingCallbackCaptor.capture());
    }

    private void setRecording() {
        // Given an initialized ViewModel with an active recording set from outside.
        viewModel.setScheduledRecording(scheduledRecording);
    }

}
