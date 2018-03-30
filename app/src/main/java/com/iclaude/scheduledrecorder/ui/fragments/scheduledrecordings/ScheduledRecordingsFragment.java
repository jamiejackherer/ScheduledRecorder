/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.ScheduledRecordingService;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsActivity;
import com.iclaude.scheduledrecorder.utils.PermissionsManager;
import com.melnykov.fab.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;

/**
 * This Fragment shows all scheduled recordings using a CalendarView.
 * <p>
 * Created by iClaude on 16/08/2017.
 */

public class ScheduledRecordingsFragment extends Fragment implements ScheduledRecordingsFragmentItemAdapter.MyOnItemClickListener {

    private final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final String ARG_POSITION = "position";
    private static final int REQUEST_DANGEROUS_PERMISSIONS = 0;
    private final boolean marshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private CompactCalendarView calendarView;
    private TextView tvMonth;
    private TextView tvDate;

    private ScheduledRecordingsViewModel viewModel;
    private RecyclerView.Adapter adapter;
    private List<ScheduledRecording> scheduledRecordings;
    private Date selectedDate = new Date(System.currentTimeMillis());

    public static ScheduledRecordingsFragment newInstance(int position) {
        ScheduledRecordingsFragment f = new ScheduledRecordingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(ScheduledRecordingsViewModel.class);
        viewModel.getScheduledRecordings().observe(this, scheduledRecordings -> {
            updateUI(scheduledRecordings);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scheduled_recordings, container, false);

        // Title.
        tvMonth = v.findViewById(R.id.tvMonth);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvMonth.setText(month);
        // Calendar view.
        calendarView = v.findViewById(R.id.compactcalendar_view);
        calendarView.setListener(myCalendarViewListener);
        // List of events for the selected day.
        RecyclerView recyclerView = v.findViewById(R.id.rvRecordings);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        scheduledRecordings = new ArrayList<>();
        adapter = new ScheduledRecordingsFragmentItemAdapter(scheduledRecordings, this, recyclerView);
        recyclerView.setAdapter(adapter);
        // Selected day.
        tvDate = v.findViewById(R.id.tvDate);
        // Add new scheduled recording button.
        FloatingActionButton mRecordButton = v.findViewById(R.id.fab_add);
        mRecordButton.setColorNormal(ContextCompat.getColor(getActivity(), R.color.primary));
        mRecordButton.setColorPressed(ContextCompat.getColor(getActivity(), R.color.primary_dark));
        mRecordButton.setOnClickListener(addScheduledRecordingListener);

        myCalendarViewListener.onDayClick(selectedDate);// click to show current day
        calendarView.setCurrentDate(selectedDate);

        return v;
    }

    // Listener for the CompactCalendarView.
    private final CompactCalendarView.CompactCalendarViewListener myCalendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date date) {
            selectedDate = date;
            displayScheduledRecordings(date);
        }

        @Override
        public void onMonthScroll(Date date) {
            DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
            String month = dateFormat.format(date);
            tvMonth.setText(month);
        }
    };

    // Display the list of scheduled recordings for the selected day.
    private void displayScheduledRecordings(Date date) {
        List<Event> events = calendarView.getEvents(date.getTime());
        // Put the events in a list of ScheduledRecording suitable for the adapter.
        scheduledRecordings.clear();
        for (Event event : events) {
            scheduledRecordings.add((ScheduledRecording) event.getData());
        }
        Collections.sort(scheduledRecordings);
        ((ScheduledRecordingsFragmentItemAdapter) adapter).setItems(scheduledRecordings);
        adapter.notifyDataSetChanged();

        tvDate.setText(new SimpleDateFormat("EEEE d", Locale.getDefault()).format(date));
    }


    // Update the UI with the list of scheduled recordings.
    private void updateUI(List<ScheduledRecording> scheduledRecordings) {
        calendarView.removeAllEvents();
        for (ScheduledRecording item : scheduledRecordings) {
            Event event = new Event(ContextCompat.getColor(getActivity(), R.color.accent), item.getStart(), item);
            calendarView.addEvent(event, false);
        }
        calendarView.invalidate(); // refresh the calendar view
        myCalendarViewListener.onDayClick(selectedDate); // click to show current day
    }

    // Click listener for the elements of the RecyclerView (for editing scheduled recordings).
    @Override
    public void onItemClick(ScheduledRecording item) {
        Intent intent = ScheduledRecordingDetailsActivity.makeIntent(getActivity(), item.getId());
        startActivity(intent);
    }

    // Long click listener for the elements of the RecyclerView (for deleting or renaming scheduled recordings).
    @Override
    public void onItemLongClick(ScheduledRecording item) {
        // Item delete confirm
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_delete));
        builder.setMessage(R.string.dialog_text_delete_generic);
        builder.setPositiveButton(R.string.dialog_action_ok,
                (dialogInterface, i) -> viewModel.deleteScheduledRecording(item, new OperationResult() {
                    @Override
                    public void onSuccess() {
                        deleteItemCompleted(true);
                    }

                    @Override
                    public void onFailure() {
                        deleteItemCompleted(false);
                    }
                }));
        builder.setCancelable(true);
        builder.setNegativeButton(getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // The item has just been deleted.
    private void deleteItemCompleted(boolean success) {
        Activity activity = getActivity();
        if (success) {
            Toast.makeText(activity, activity.getString(R.string.toast_scheduledrecording_deleted), Toast.LENGTH_SHORT).show();
            activity.startService(ScheduledRecordingService.makeIntent(activity, false));
        } else {
            Toast.makeText(activity, activity.getString(R.string.toast_scheduledrecording_deleted_error), Toast.LENGTH_SHORT).show();
        }
    }

    // Click listener of the button to add a new scheduled recording.
    private final View.OnClickListener addScheduledRecordingListener = view -> checkPermissionsAndSchedule();

    // Check dangerous permissions for Android Marshmallow+.
    private void checkPermissionsAndSchedule() {
        if(!marshmallow) {
            startScheduledRecordingDetailsActivity();
            return;
        }

        String[] permissionsToAsk = PermissionsManager.checkPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
        if(permissionsToAsk.length > 0)
            requestPermissions(permissionsToAsk, REQUEST_DANGEROUS_PERMISSIONS);
        else
            startScheduledRecordingDetailsActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) { // we nee all permissions granted
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                granted = false;
        }

        if (granted)
            startActivity(ScheduledRecordingDetailsActivity.makeIntent(getActivity(), selectedDate.getTime()));
        else
            Toast.makeText(getActivity(), getString(R.string.toast_permissions_denied), Toast.LENGTH_LONG).show();
    }

    private void startScheduledRecordingDetailsActivity() {
        Intent intent = ScheduledRecordingDetailsActivity.makeIntent(getActivity(), selectedDate.getTime());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
