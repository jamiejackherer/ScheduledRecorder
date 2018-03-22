/*
 * Year: 2017. This class was added by iClaude.
 */

package com.iclaude.scheduledrecorder;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.iclaude.scheduledrecorder.database.AppDatabase;
import com.iclaude.scheduledrecorder.database.RecordingsDao;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for "scheduled_recordings" table in the database.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DBScheduledRecordingsTest {

    private AppDatabase appDatabase;
    private RecordingsDao recordingsDao;

    private static final ScheduledRecording RECORDING = new ScheduledRecording(25, 15000, 55000);


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
        recordingsDao.insertScheduledRecording(RECORDING);

        // When getting the recording by id from the database
        ScheduledRecording loaded = recordingsDao.getScheduledRecordingById(RECORDING.getId());

        // The loaded data contains the expected values
        assertRecording(loaded, 25, 15000, 55000);
    }

    @Test
    public void insertRecordingReplacesOnConflict() {
        //Given that a recording is inserted
        recordingsDao.insertScheduledRecording(RECORDING);

        // When a recording with the same id is inserted
        ScheduledRecording newRecording = new ScheduledRecording(25, 10000, 20000);
        recordingsDao.insertScheduledRecording(newRecording);
        // When getting the recording by id from the database
        ScheduledRecording loaded = recordingsDao.getScheduledRecordingById(RECORDING.getId());

        // The loaded data contains the expected values
        assertRecording(loaded, 25, 10000, 20000);
    }

    @Test
    public void insertRecordingAndGetRecordings() throws Exception {
        // When inserting a recording
        recordingsDao.insertScheduledRecording(RECORDING);

        // When getting the recordings from the database
        LiveData<List<ScheduledRecording>> myLiveData = recordingsDao.getAllScheduledRecordings();
        List<ScheduledRecording> recordings = LiveDataTestUtil.getValue(myLiveData);

        // There is only 1 recording in the database
        assertThat(recordings.size(), is(1));
        // The loaded data contains the expected values
        assertRecording(recordings.get(0), 25, 15000, 55000);
    }

    @Test
    public void updateRecordingAndGetById() {
        // When inserting a recording
        recordingsDao.insertScheduledRecording(RECORDING);

        // When the recording is updated
        ScheduledRecording updatedRecording = new ScheduledRecording(25, 10000, 20000);
        recordingsDao.updateScheduledRecordings(updatedRecording);

        // When getting the recording by id from the database
        ScheduledRecording loaded = recordingsDao.getScheduledRecordingById(25);

        // The loaded data contains the expected values
        assertRecording(loaded, 25, 10000, 20000);
    }

    @Test
    public void deleteRecordingByIdAndGettingRecordings() throws Exception {
        //Given a recording inserted
        recordingsDao.insertScheduledRecording(RECORDING);

        //When deleting a recording by id
        recordingsDao.deleteScheduledRecordings(RECORDING);

        //When getting the recordings
        LiveData<List<ScheduledRecording>> myLiveData = recordingsDao.getAllScheduledRecordings();
        List<ScheduledRecording> recordings = LiveDataTestUtil.getValue(myLiveData);
        // The list is empty
        assertThat(recordings.size(), is(0));
    }

    @Test
    public void testAddUpdateDelete() throws Exception {
        assertEquals("Table is not empty", 0, recordingsDao.getScheduledRecordingsCount());

        // Add.
        recordingsDao.insertScheduledRecording(new ScheduledRecording(0, 100));
        assertEquals("Records not incremented to 1 after 1st insertion", 1, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 500));
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, recordingsDao.getScheduledRecordingsCount());
        ScheduledRecording scheduledRecording = new ScheduledRecording(200, 600);
        long id = recordingsDao.insertScheduledRecording(scheduledRecording);
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, recordingsDao.getScheduledRecordingsCount());

        // Update.
        scheduledRecording = recordingsDao.getScheduledRecordingById((int) id);
        scheduledRecording.setStart(250);
        scheduledRecording.setEnd(650);
        int updated = recordingsDao.updateScheduledRecordings(scheduledRecording);
        assertEquals("Number of updated records should be 1 but was " + updated, 1, updated);
        scheduledRecording = recordingsDao.getScheduledRecordingById((int) id);
        assertNotNull("Item is null", scheduledRecording);
        assertEquals("Start time should be 250", 250, scheduledRecording.getStart());
        assertEquals("End time should be 650", 650, scheduledRecording.getEnd());

        // Delete.
        int deleted = recordingsDao.deleteScheduledRecordings(scheduledRecording);
        assertEquals("Number of deleted records should be 1 but was " + deleted, 1, deleted);
        assertEquals("Records not decreased to 2 after deletion", 2, recordingsDao.getScheduledRecordingsCount());
    }

    @Test
    public void testGetNextScheduledRecording() throws Exception {
        recordingsDao.insertScheduledRecording(new ScheduledRecording(200, 250));
        recordingsDao.insertScheduledRecording(new ScheduledRecording(150, 190));
        recordingsDao.insertScheduledRecording(new ScheduledRecording(300, 400));

        ScheduledRecording scheduledRecording = recordingsDao.getNextScheduledRecording();
        assertNotNull("Item is null", scheduledRecording);
        assertEquals("Start time should be 150", 150, scheduledRecording.getStart());
        assertEquals("End time should be 190", 190, scheduledRecording.getEnd());
    }

    @Test
    public void testGetNumScheduledRecordingsAtTime() throws Exception {
        recordingsDao.insertScheduledRecording(new ScheduledRecording(200, 300));
        int num = recordingsDao.getNumRecordingsAlreadyScheduled(250, 280, -1);
        assertEquals("Num of scheduled recordings should be 1, but was " + num, 1, num);
        num = recordingsDao.getNumRecordingsAlreadyScheduled(150, 160, -1);
        assertEquals("Num of scheduled recordings should be 0, but was " + num, 0, num);
    }

    @Test
    public void testGetScheduledRecordingsBetween() throws Exception {
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 200));
        recordingsDao.insertScheduledRecording(new ScheduledRecording(150, 500));
        recordingsDao.insertScheduledRecording(new ScheduledRecording(300, 350));
        List<ScheduledRecording> scheduledRecordings = recordingsDao.getScheduledRecordingsBetween(100, 160);
        assertEquals("Num of recordings should be 2, but was " + scheduledRecordings.size(), 2, scheduledRecordings.size());
    }

    private void assertRecording(ScheduledRecording recording, int id, long start, long end) {
        assertThat(recording, notNullValue());
        assertThat(recording.getId(), is(id));
        assertThat(recording.getStart(), is(start));
        assertThat(recording.getEnd(), is(end));
    }
}
