package com.iclaude.scheduledrecorder.scheduledrecordings;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.google.common.collect.Lists;
import com.iclaude.scheduledrecorder.testutils.LiveDataTestUtil;
import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.testutils.TestUtils;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings.ScheduledRecordingsViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScheduledRecordingsViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private RecordingsRepository repository;
    @Mock
    private Application mContext;
    @Captor
    private ArgumentCaptor<RecordingsRepositoryInterface.OperationResult> operationResultCaptor;

    private final ScheduledRecording SCHEDULED_RECORDING = new ScheduledRecording(1, 1000, 2000);
    private final List<ScheduledRecording> SCHEDULED_RECORDINGS = Lists.newArrayList(
            new ScheduledRecording(1, 1523217600000L, 1523219400000L), // 8 april 2008: 20:00-20:30
            new ScheduledRecording(2, 1523262600000L, 1523263500000L), // 9 april 2008: 8:30-8:45
            new ScheduledRecording(3, 1523529900000L, 1523531100000L) // 12 april 2008: 10:45-11:05
    );
    private final List<ScheduledRecording> SCHEDULED_RECORDINGS_TEST = Lists.newArrayList(
            new ScheduledRecording(2, 1523262600000L, 1523263500000L), // 9 april 2008: 8:30-8:45
            new ScheduledRecording(3, 1523529900000L, 1523531100000L) // 12 april 2008: 10:45-11:05
    );

    private ScheduledRecordingsViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        viewModel = new ScheduledRecordingsViewModel(repository);
    }

    @Test
    public void testAddCommand() {
        Observer<Void> observer = mock(Observer.class);
        viewModel.getAddCommand().observe(TestUtils.TEST_OBSERVER, observer);
        viewModel.addScheduledRecording();
        verify(observer).onChanged(null);
    }

    @Test
    public void testEditCommand() {
        Observer<ScheduledRecording> observer = mock(Observer.class);
        viewModel.getEditCommand().observe(TestUtils.TEST_OBSERVER, observer);
        viewModel.editScheduledRecording(SCHEDULED_RECORDING);
        verify(observer).onChanged(SCHEDULED_RECORDING);
    }

    @Test
    public void testDeleteRecordingOK() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getDeleteCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of successful recording deletion.
        viewModel.deleteScheduledRecording(SCHEDULED_RECORDING);
        verify(repository).deleteScheduledRecording(eq(SCHEDULED_RECORDING), operationResultCaptor.capture());
        operationResultCaptor.getValue().onSuccess();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_recording_deleted);
    }

    @Test
    public void testDeleteRecordingError() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getDeleteCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of failure in deletion.
        viewModel.deleteScheduledRecording(SCHEDULED_RECORDING);
        verify(repository).deleteScheduledRecording(eq(SCHEDULED_RECORDING), operationResultCaptor.capture());
        operationResultCaptor.getValue().onFailure();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_recording_deleted_error);
    }

    @Test
    public void testGetScheduledRecordingsOK() throws Exception {
        loadScheduledRecordings(SCHEDULED_RECORDINGS);
        assertFalse(viewModel.dataLoading.get());
        assertFalse(viewModel.dataAvailable.get());


        Observer<List<ScheduledRecording>> observer = mock(Observer.class);
        viewModel.getScheduledRecordings().observe(TestUtils.TEST_OBSERVER, observer);
        LiveData<List<ScheduledRecording>> recordingsLive = viewModel.getScheduledRecordings();
        List<ScheduledRecording> recordings = LiveDataTestUtil.getValue(recordingsLive);

        assertNotNull(recordings);
        assertEquals(3, recordings.size());
        assertEquals(1, recordings.get(0).getId());
        assertEquals(1523262600000L, recordings.get(1).getStart());
        assertEquals(1523531100000L, recordings.get(2).getEnd());

        assertFalse(viewModel.dataLoading.get());

        verify(observer).onChanged(SCHEDULED_RECORDINGS);
    }

    @Test
    public void testScheduledRecordingsFiltered() throws Exception {
        loadScheduledRecordings(SCHEDULED_RECORDINGS);
        viewModel.getScheduledRecordings();

        Observer<List<ScheduledRecording>> observer = mock(Observer.class);
        viewModel.getScheduledRecordingsFiltered().observe(TestUtils.TEST_OBSERVER, observer);

        // Test that the filters work correctly.
        viewModel.setSelectedDate(new Date(1523531100000L)); // 12 april
        LiveData<List<ScheduledRecording>> listFilteredLive = viewModel.getScheduledRecordingsFiltered();
        List<ScheduledRecording> listFiltered = LiveDataTestUtil.getValue(listFilteredLive);
        assertNotNull(listFiltered);
        assertEquals(1, listFiltered.size());
        assertEquals(3, listFiltered.get(0).getId());
        assertEquals(1523529900000L, listFiltered.get(0).getStart());
        assertEquals(1523531100000L, listFiltered.get(0).getEnd());

        // Test that the observer receives the filtered list.
        verify(observer).onChanged(listFiltered);
    }

    @Test
    public void testUseCachedList() throws Exception {
        loadScheduledRecordings(SCHEDULED_RECORDINGS);
        viewModel.getScheduledRecordings(); // returns SCHEDULED_RECORDINGS

        MutableLiveData<List<ScheduledRecording>> scheduledRecordingsLive = new MutableLiveData<>();
        scheduledRecordingsLive.setValue(SCHEDULED_RECORDINGS_TEST);
        when(repository.getAllScheduledRecordings()).thenReturn(scheduledRecordingsLive);

        Observer<List<ScheduledRecording>> observer = mock(Observer.class);
        viewModel.getScheduledRecordings().observe(TestUtils.TEST_OBSERVER, observer);

        verify(observer).onChanged(SCHEDULED_RECORDINGS); // should return SCHEDULED_RECORDINGS, and not SCHEDULED_RECORDINGS_TEST, because the list is cached
    }

    // Return our test list when calling repository.getAllScheduledRecordings().
    private void loadScheduledRecordings(List<ScheduledRecording> scheduledRecordings) {
        MutableLiveData<List<ScheduledRecording>> scheduledRecordingsLive = new MutableLiveData<>();
        scheduledRecordingsLive.setValue(scheduledRecordings);
        when(repository.getAllScheduledRecordings()).thenReturn(scheduledRecordingsLive);
    }
}
