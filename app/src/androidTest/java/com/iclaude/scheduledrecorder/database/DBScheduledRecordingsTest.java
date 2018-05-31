package com.iclaude.scheduledrecorder.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.iclaude.scheduledrecorder.testutils.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
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
        assertRecording(loaded, RECORDING.getId(), RECORDING.getStart(), RECORDING.getEnd());
    }

    @Test
    public void insertRecordingReplacesOnConflict() {
        // Given that a recording is inserted
        recordingsDao.insertScheduledRecording(RECORDING);

        // When a recording with the same id is inserted
        ScheduledRecording newRecording = new ScheduledRecording(25, 10000, 20000);
        recordingsDao.insertScheduledRecording(newRecording);
        // When getting the recording by id from the database
        ScheduledRecording loaded = recordingsDao.getScheduledRecordingById(RECORDING.getId());

        // The loaded data contains the expected values
        assertRecording(loaded, newRecording.getId(), newRecording.getStart(), newRecording.getEnd());
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
        assertRecording(recordings.get(0), RECORDING.getId(), RECORDING.getStart(), RECORDING.getEnd());
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
        assertRecording(loaded, updatedRecording.getId(), updatedRecording.getStart(), updatedRecording.getEnd());
    }

    @Test
    public void deleteRecordingByIdAndGettingRecordings() throws Exception {
        // Given a recording inserted
        recordingsDao.insertScheduledRecording(RECORDING);

        // When deleting a recording by id
        recordingsDao.deleteScheduledRecording(RECORDING);

        // When getting the recordings
        LiveData<List<ScheduledRecording>> myLiveData = recordingsDao.getAllScheduledRecordings();
        List<ScheduledRecording> recordings = LiveDataTestUtil.getValue(myLiveData);
        // The list is empty
        assertThat(recordings.size(), is(0));
    }

    @Test
    public void testAddUpdateDelete() throws Exception {
        assertEquals("Table is not empty", 0, recordingsDao.getScheduledRecordingsCount());

        // Add 3 recordings.
        recordingsDao.insertScheduledRecording(new ScheduledRecording(0, 100));
        assertEquals("Records not incremented to 1 after 1st insertion", 1, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 500));
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, recordingsDao.getScheduledRecordingsCount());
        ScheduledRecording scheduledRecording = new ScheduledRecording(200, 600);
        long id = recordingsDao.insertScheduledRecording(scheduledRecording);
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, recordingsDao.getScheduledRecordingsCount());

        // Update.
        scheduledRecording = recordingsDao.getScheduledRecordingById(id);
        scheduledRecording.setStart(250);
        scheduledRecording.setEnd(650);
        int updated = recordingsDao.updateScheduledRecordings(scheduledRecording);
        assertEquals("Number of updated records should be 1 but was " + updated, 1, updated);
        scheduledRecording = recordingsDao.getScheduledRecordingById(id);
        assertNotNull("Item is null", scheduledRecording);
        assertEquals("Start time should be 250", 250, scheduledRecording.getStart());
        assertEquals("End time should be 650", 650, scheduledRecording.getEnd());

        // Delete.
        int deleted = recordingsDao.deleteScheduledRecording(scheduledRecording);
        assertEquals("Number of deleted records should be 1 but was " + deleted, 1, deleted);
        assertEquals("Records not decreased to 2 after deletion", 2, recordingsDao.getScheduledRecordingsCount());
    }

    @Test
    public void testDeleteAllRecordings() throws Exception {
        assertEquals("Table is not empty", 0, recordingsDao.getScheduledRecordingsCount());

        // Add 3 recordings.
        recordingsDao.insertScheduledRecording(new ScheduledRecording(0, 100));
        assertEquals("Records not incremented to 1 after 1st insertion", 1, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 500));
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(200, 600));
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, recordingsDao.getScheduledRecordingsCount());

        // Delete all recordings.
        recordingsDao.deleteAllScheduledRecordings();

        // Check that the database is now empty.
        assertEquals("Table is not empty", 0, recordingsDao.getScheduledRecordingsCount());
    }

    @Test
    public void testDeleteOldRecordings() throws Exception {
        assertEquals("Table is not empty", 0, recordingsDao.getScheduledRecordingsCount());

        // Add 3 recordings.
        recordingsDao.insertScheduledRecording(new ScheduledRecording(300, 350));
        assertEquals("Records not incremented to 1 after 1st insertion", 1, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 500));
        assertEquals("Records not incremented to 2 after 2nd insertion", 2, recordingsDao.getScheduledRecordingsCount());
        recordingsDao.insertScheduledRecording(new ScheduledRecording(200, 600));
        assertEquals("Records not incremented to 3 after 3rd insertion", 3, recordingsDao.getScheduledRecordingsCount());

        // Delete recordings older than 150.
        recordingsDao.deleteOldScheduledRecordings(150);

        // When getting the recordings from the database
        LiveData<List<ScheduledRecording>> myLiveData = recordingsDao.getAllScheduledRecordings();
        List<ScheduledRecording> recordings = LiveDataTestUtil.getValue(myLiveData);
        // Check that old recordings have been deleted.
        assertThat(recordings.size(), is(2));
        ScheduledRecording recording = recordings.get(0);
        assertTrue("Old recording was not deleted", (recording.getStart() - 150) >= 0);
        recording = recordings.get(1);
        assertTrue("Old recording was not deleted", (recording.getStart() - 150) >= 0);

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
    public void testGetScheduledRecordingsAlreadyScheduled() throws Exception {
        // Add 3 scheduled recordings.
        recordingsDao.insertScheduledRecording(new ScheduledRecording(100, 200));
        recordingsDao.insertScheduledRecording(new ScheduledRecording(300, 350));
        long id = recordingsDao.insertScheduledRecording(new ScheduledRecording(150, 500));

        // Check if at different times other recordings have already been scheduled.
        int numScheduled = recordingsDao.getNumRecordingsAlreadyScheduled(50, 70, -1);
        assertThat("Error: this times are free", numScheduled, is(0));
        numScheduled = recordingsDao.getNumRecordingsAlreadyScheduled(90, 110, -1);
        assertThat("Error: this times are not free", numScheduled, is(1));
        numScheduled = recordingsDao.getNumRecordingsAlreadyScheduled(310, 315, -1);
        assertThat("Error: this times are not free", numScheduled, is(2));
        numScheduled = recordingsDao.getNumRecordingsAlreadyScheduled(480, 515, -1);
        assertThat("Error: this times are not free", numScheduled, is(1));
        numScheduled = recordingsDao.getNumRecordingsAlreadyScheduled(480, 515, id);
        assertThat("Error: this times are free", numScheduled, is(0));
    }

    private void assertRecording(ScheduledRecording recording, int id, long start, long end) {
        assertThat(recording, notNullValue());
        assertThat(recording.getId(), is(id));
        assertThat(recording.getStart(), is(start));
        assertThat(recording.getEnd(), is(end));
    }
}
