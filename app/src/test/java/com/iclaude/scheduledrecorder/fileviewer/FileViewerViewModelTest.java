package com.iclaude.scheduledrecorder.fileviewer;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.google.common.collect.Lists;
import com.iclaude.scheduledrecorder.testutils.LiveDataTestUtil;
import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.testutils.TestUtils;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.ui.fragments.fileviewer.FileViewerViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of FileViewerViewModel.
 */

public class FileViewerViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private final List<Recording> RECORDINGS = Lists.newArrayList(
            new Recording(1, "name1", "path1", 1000, 11000),
            new Recording(2, "name2", "path2", 2000, 12000),
            new Recording(3, "name3", "path3", 3000, 13000));
    private final List<Recording> RECORDINGS_EMPTY = new ArrayList<>();

    private final Recording RECORDING = new Recording(1, "name1", "path1", 1000, 10000);

    private FileViewerViewModel viewModel;

    @Mock
    private RecordingsRepository repository;
    @Mock
    private Application mContext;
    @Captor
    private ArgumentCaptor<RecordingsRepositoryInterface.OperationResult> operationResultCaptor;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        viewModel = new FileViewerViewModel(mContext, repository);
        //viewModel.getRecordings().removeObservers(TestUtils.TEST_OBSERVER);

    }

    @Test
    public void testGetRecordingsOK() throws Exception {
        loadRecordings(RECORDINGS);
        assertFalse(viewModel.dataLoading.get());
        assertFalse(viewModel.dataAvailable.get());

        Observer<List<Recording>> observer = mock(Observer.class);
        viewModel.getRecordings().observe(TestUtils.TEST_OBSERVER, observer);
        LiveData<List<Recording>> recordingsLive = repository.getAllRecordings();
        List<Recording> recordings = LiveDataTestUtil.getValue(recordingsLive);

        assertNotNull(recordings);
        assertEquals(3, recordings.size());
        assertEquals("name1", recordings.get(0).getName());
        assertEquals("path2", recordings.get(1).getPath());
        assertEquals(13000, recordings.get(2).getTimeAdded());

        assertFalse(viewModel.dataLoading.get());
        assertTrue(viewModel.dataAvailable.get());

        verify(observer).onChanged(RECORDINGS);
    }

    @Test
    public void testGetRecordingsError() throws Exception {
        loadRecordings(RECORDINGS_EMPTY);
        assertFalse(viewModel.dataLoading.get());
        assertFalse(viewModel.dataAvailable.get());

        Observer<List<Recording>> observer = mock(Observer.class);
        viewModel.getRecordings().observe(TestUtils.TEST_OBSERVER, observer);
        LiveData<List<Recording>> recordingsLive = repository.getAllRecordings();
        List<Recording> recordings = LiveDataTestUtil.getValue(recordingsLive);

        assertNotNull(recordings);
        assertFalse(viewModel.dataLoading.get());
        assertFalse(viewModel.dataAvailable.get());

        verify(observer).onChanged(RECORDINGS_EMPTY);
    }

    @Test
    public void testPlayRecording() {
        Observer<Recording> observer = mock(Observer.class);
        viewModel.getPlayRecordingEvent().observe(TestUtils.TEST_OBSERVER, observer);
        viewModel.playRecording(RECORDING);
        verify(observer).onChanged(RECORDING);
    }

    @Test
    public void testShowLongClickDialogOptions() {
        Observer<Recording> observer = mock(Observer.class);
        viewModel.getLongClickItemEvent().observe(TestUtils.TEST_OBSERVER, observer);
        viewModel.showLongClickDialogOptions(RECORDING);
        verify(observer).onChanged(RECORDING);
    }

    @Test
    public void testDeleteRecordingOK() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getDeleteCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of successful recording deletion.
        viewModel.deleteRecording(RECORDING);
        verify(repository).deleteRecording(eq(RECORDING), operationResultCaptor.capture());
        operationResultCaptor.getValue().onSuccess();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_recording_deleted);
    }

    @Test
    public void testDeleteRecordingError() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getDeleteCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of recording deletion with error.
        viewModel.deleteRecording(RECORDING);
        verify(repository).deleteRecording(eq(RECORDING), operationResultCaptor.capture());
        operationResultCaptor.getValue().onFailure();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_recording_deleted_error);
    }

    @Test
    public void testUpdateRecordingOK() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getUpdateCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of successful recording update.
        viewModel.updateRecording(RECORDING, "new_name");
        verify(repository).updateRecording(eq(RECORDING), eq("new_name"), eq(mContext), operationResultCaptor.capture());
        operationResultCaptor.getValue().onSuccess();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_file_renamed);
    }

    @Test
    public void testUpdateRecordingError() {
        // Set up observer.
        Observer<Integer> observer = mock(Observer.class);
        viewModel.getUpdateCommand().observe(TestUtils.TEST_OBSERVER, observer);

        // Simulation of recording update with error.
        viewModel.updateRecording(RECORDING, "new_name");
        verify(repository).updateRecording(eq(RECORDING), eq("new_name"), eq(mContext), operationResultCaptor.capture());
        operationResultCaptor.getValue().onFailure();

        // Verify the value received by the observer.
        verify(observer).onChanged(R.string.toast_file_renamed_error);
    }

    private void loadRecordings(List<Recording> recordings) {
        MutableLiveData<List<Recording>> recordingsLive = new MutableLiveData<>();
        recordingsLive.setValue(recordings);
        when(repository.getAllRecordings()).thenReturn(recordingsLive);
    }
}
