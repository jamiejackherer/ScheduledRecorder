package com.iclaude.scheduledrecorder.ui.activities.scheduled_recording;

import android.app.DialogFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.ScheduledRecordingService;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.databinding.ActivityScheduledRecordingDetailsBinding;
import com.iclaude.scheduledrecorder.ui.fragments.DatePickerFragment;
import com.iclaude.scheduledrecorder.ui.fragments.TimePickerFragment;
import com.iclaude.scheduledrecorder.utils.Utils;

import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE.DATE_END;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.DATE_TYPE.DATE_START;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.OPERATION;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.OPERATION.ADD;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.OPERATION.EDIT;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.RESULT.ERROR;
import static com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsViewModel.TIME_TYPE;

/**
 * Activity for displaying and editing a scheduled recording's data.
 */

public class ScheduledRecordingDetailsActivity extends AppCompatActivity implements DatePickerFragment.MyOnDateSetListener, TimePickerFragment.MyOnTimeSetListener, ScheduledRecordingDetailsNavigator {

    private static final String EXTRA_RECORDING_ID = "com.iclaude.scheduledrecorder.EXTRA_RECORDING_ID";
    private static final String EXTRA_SELECTED_DATE = "com.iclaude.scheduledrecorder.EXTRA_SELECTED_DATE";

    private ScheduledRecordingDetailsViewModel viewModel;
    private int id;
    private long selectedDate;
    private boolean dataLoaded;
    private OPERATION operation;

    // Edit (existing recording with id).
    public static Intent makeIntent(Context context, int id) {
        Intent intent = new Intent(context, ScheduledRecordingDetailsActivity.class);
        intent.putExtra(EXTRA_RECORDING_ID, id);
        return intent;
    }

    // Add (at specified date).
    public static Intent makeIntent(Context context, long selectedDate) {
        Intent intent = new Intent(context, ScheduledRecordingDetailsActivity.class);
        intent.putExtra(EXTRA_SELECTED_DATE, selectedDate);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getIntent().getIntExtra(EXTRA_RECORDING_ID, -1);
        selectedDate = getIntent().getLongExtra(EXTRA_SELECTED_DATE, 0);
        operation = id == -1 ? ADD : EDIT;

        // ViewModel and data binding.
        viewModel = ViewModelProviders.of(this)
                .get(ScheduledRecordingDetailsViewModel.class);
        ActivityScheduledRecordingDetailsBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_scheduled_recording_details);
        binding.setViewModel(viewModel);
        binding.setListener(pickerListener);

        // Single live event: user clicks save button in the action bar.
        viewModel.getSaveTaskCommand().observe(this, ScheduledRecordingDetailsActivity.this::onScheduledRecordingSaved);
        // Single live event: the recording was dataLoaded.
        viewModel.getLoadedCommand().observe(this, aVoid -> ScheduledRecordingDetailsActivity.this.onScheduledRecordingLoaded());

        // Action bar (Toolbar).
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false); // hide the title
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (operation == EDIT)
            viewModel.loadScheduledRecordingById(id);
        else
            viewModel.setScheduledRecording(new ScheduledRecording(selectedDate, selectedDate + 1000L * 60 * 30));
    }

    @Override
    public void onScheduledRecordingLoaded() {
        dataLoaded = true;
        invalidateOptionsMenu();
    }

    // Select dates and times.
    private final PickerListener pickerListener = new PickerListener() {
        @Override
        public void showDatePickerDialog(View view) {
            long date = view.getId() == R.id.tvDateStart ? viewModel.scheduledRecordingObservable.get().getStart() : viewModel.scheduledRecordingObservable.get().getEnd();
            DialogFragment datePicker = DatePickerFragment.newInstance(view.getId(), date);
            datePicker.show(getFragmentManager(), "datePicker");
        }

        @Override
        public void showTimePickerDialog(View view) {
            int hour = 0, minute = 0;
            ScheduledRecording scheduledRecording = viewModel.scheduledRecordingObservable.get();
            switch (view.getId()) {
                case R.id.tvTimeStart:
                    hour = Utils.getHourFromTimeMillis(scheduledRecording.getStart());
                    minute = Utils.getMinuteFromTimeMillis(scheduledRecording.getStart());
                    break;
                case R.id.tvTimeEnd:
                    hour = Utils.getHourFromTimeMillis(scheduledRecording.getEnd());
                    minute = Utils.getMinuteFromTimeMillis(scheduledRecording.getEnd());
                    break;
            }

            DialogFragment timePicker = TimePickerFragment.newInstance(view.getId(), hour, minute);
            timePicker.show(getFragmentManager(), "timePicker");
        }
    };

    // Callback methods for DatePickerFragment and TimePickerFragment.
    @Override
    public void onDateSet(long viewId, int year, int month, int day) {
        DATE_TYPE dateType = viewId == R.id.tvDateStart ? DATE_START : DATE_END;
        viewModel.setDate(dateType, year, month, day);
    }

    @Override
    public void onTimeSet(long viewId, int hour, int minute) {
        TIME_TYPE timeType = viewId == R.id.tvTimeStart ? TIME_TYPE.TIME_START : TIME_TYPE.TIME_END;
        viewModel.setTime(timeType, hour, minute);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_save).setEnabled(dataLoaded);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scheduledrecordingdetails, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                viewModel.saveScheduledRecording(operation);
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onScheduledRecordingSaved(RESULT result) {
        // Errors.
        if (result == ERROR) {
            Toast.makeText(this, getString(R.string.toast_scheduledrecording_saved_error), Toast.LENGTH_SHORT).show();
            return;
        }

        // Recording saved.
        Toast.makeText(ScheduledRecordingDetailsActivity.this, getString(R.string.toast_recording_saved), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        startService(ScheduledRecordingService.makeIntent(ScheduledRecordingDetailsActivity.this, false));
        finish();
    }
}
