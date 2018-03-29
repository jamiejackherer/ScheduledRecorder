/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.iclaude.scheduledrecorder.database.AppDatabase;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Tests for "saved_recordings" table in the database.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DBSavedRecordingsTest {

    private AppDatabase appDatabase;
    private RecordingsDao recordingsDao;

    private static final Recording RECORDING = new Recording(25, "recording_name", "recording_path", 15000L, 55000L);

    @Before
    public void createDatabase() {
        Context context = InstrumentationRegistry.getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        recordingsDao = appDatabase.recordingsDao();
    }

    @After
    public void closeDatabase() throws IOException {
        appDatabase.close();
    }

    @Test
    public void insertRecordingAndGetById() {
        // When inserting a recording
        recordingsDao.insertRecording(RECORDING);

        // When getting the recording by id from the database
        Recording loaded = recordingsDao.getRecordingById(RECORDING.getId());

        // The loaded data contains the expected values
        assertRecording(loaded, 25, "recording_name", "recording_path", 15000, 55000);
    }

    @Test
    public void insertRecordingReplacesOnConflict() {
        //Given that a recording is inserted
        recordingsDao.insertRecording(RECORDING);

        // When a recording with the same id is inserted
        Recording newRecording = new Recording(25, "new_recording_name", "new_recording_path", 10000, 20000);
        recordingsDao.insertRecording(newRecording);
        // When getting the recording by id from the database
        Recording loaded = recordingsDao.getRecordingById(RECORDING.getId());

        // The loaded data contains the expected values
        assertRecording(loaded, 25, "new_recording_name", "new_recording_path", 10000, 20000);
    }

    @Test
    public void insertRecordingAndGetRecordings() throws Exception {
        // When inserting a recording
        recordingsDao.insertRecording(RECORDING);

        // When getting the recordings from the database
        LiveData<List<Recording>> myLiveData = recordingsDao.getAllRecordings();
        List<Recording> recordings = LiveDataTestUtil.getValue(myLiveData);

        // There is only 1 recording in the database
        assertThat(recordings.size(), is(1));
        // The loaded data contains the expected values
        assertRecording(recordings.get(0), 25, "recording_name", "recording_path", 15000, 55000);
    }

    @Test
    public void updateRecordingAndGetById() {
        // When inserting a recording
        recordingsDao.insertRecording(RECORDING);

        // When the recording is updated
        Recording updatedRecording = new Recording(25, "new_recording_name", "new_recording_path", 10000, 20000);
        recordingsDao.updateRecording(updatedRecording);

        // When getting the recording by id from the database
        Recording loaded = recordingsDao.getRecordingById(25);

        // The loaded data contains the expected values
        assertRecording(loaded, 25, "new_recording_name", "new_recording_path", 10000, 20000);
    }

    @Test
    public void deleteRecordingByIdAndGettingRecordings() throws Exception {
        //Given a recording inserted
        recordingsDao.insertRecording(RECORDING);

        //When deleting a recording by id
        recordingsDao.deleteRecording(RECORDING);

        //When getting the recordings
        LiveData<List<Recording>> myLiveData = recordingsDao.getAllRecordings();
        List<Recording> recordings = LiveDataTestUtil.getValue(myLiveData);
        // The list is empty
        assertThat(recordings.size(), is(0));
    }

    @Test
    public void testAddUpdateDelete() throws Exception {
        assertThat("Table is not empty", recordingsDao.getRecordingsCount(), is(0));

        // Add.
        recordingsDao.insertRecording(new Recording("recording1", "path1", 10000, 20000));
        assertThat("Records not incremented to 1 after 1st insertion", recordingsDao.getRecordingsCount(), is(1));
        recordingsDao.insertRecording(new Recording("recording2", "path2", 30000, 50000));
        assertThat("Records not incremented to 2 after 2nd insertion", recordingsDao.getRecordingsCount(), is(2));
        recordingsDao.insertRecording(RECORDING);
        assertThat("Records not incremented to 3 after 3rd insertion", recordingsDao.getRecordingsCount(), is(3));

        // Update.
        Recording recording = recordingsDao.getRecordingById(25);
        recording.setName("new_name");
        recording.setPath("new_path");
        int updated = recordingsDao.updateRecording(recording);
        assertThat("Number of updated records should be 1 but was " + updated, updated, is(1));
        recording = recordingsDao.getRecordingById(25);
        assertThat("Item is null", recording, notNullValue());
        assertThat("Name not updated", recording.getName(), equalTo("new_name"));
        assertThat("Path not updated", recording.getPath(), equalTo("new_path"));

        // Delete.
        int deleted = recordingsDao.deleteRecording(recording);
        assertThat("Number of deleted records should be 1 but was " + deleted, deleted, is(1));
        assertThat("Records not decreased to 2 after deletion", recordingsDao.getRecordingsCount(), is(2));
    }

    private void assertRecording(Recording recording, int id, String name,
                                 String path, long length, long timeAdded) {
        assertThat(recording, notNullValue());
        assertThat(recording.getId(), is(id));
        assertThat(recording.getName(), is(name));
        assertThat(recording.getPath(), is(path));
        assertThat(recording.getLength(), is(length));
        assertThat(recording.getTimeAdded(), is(timeAdded));
    }
}
