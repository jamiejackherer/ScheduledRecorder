/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.databinding.FragmentScheduledRecordingsBinding;
import com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsActivity;
import com.iclaude.scheduledrecorder.utils.PermissionsManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * This Fragment shows all scheduled recordings using a CalendarView.
 * <p>
 * Created by iClaude on 16/08/2017.
 */

public class ScheduledRecordingsFragment extends Fragment {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private static final String ARG_POSITION = "position";
    private static final int REQUEST_DANGEROUS_PERMISSIONS = 0;
    private final boolean marshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    private CompactCalendarView calendarView;

    private ScheduledRecordingsViewModel viewModel;
    private RecyclerViewListAdapter adapter;
    private List<ScheduledRecording> scheduledRecordings = new ArrayList<>();


    public static ScheduledRecordingsFragment newInstance(int position) {
        ScheduledRecordingsFragment f = new ScheduledRecordingsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(ScheduledRecordingsViewModel.class);

        // Observables.
        viewModel.getScheduledRecordings().observe(this, scheduledRecordings -> {
            viewModel.dataAvailable.set(scheduledRecordings != null && !scheduledRecordings.isEmpty());
            this.scheduledRecordings = scheduledRecordings;
            updateCalendarView(Objects.requireNonNull(scheduledRecordings));
            viewModel.filterList();
        });

        viewModel.getScheduledRecordingsFiltered().observe(this,
                scheduledRecordings -> {
                    adapter.submitList(scheduledRecordings);
                    Log.d(TAG, "list filtered");
                }
                );

        // Commands.
        viewModel.getAddCommand().observe(this, aVoid -> checkPermissionsAndSchedule());
        viewModel.getEditCommand().observe(this, this::editScheduledRecording);
        viewModel.getLongClickItemEvent().observe(this, this::deleteScheduledRecording);
        viewModel.getDeleteCommand().observe(this, msgId -> {
            if(msgId != null)
                Toast.makeText(getActivity(),getString(msgId), Toast.LENGTH_SHORT).show();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentScheduledRecordingsBinding binding = FragmentScheduledRecordingsBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        View rootView = binding.getRoot();

        // Calendar view.
        viewModel.setSelectedDate(viewModel.selectedDate.get());
        viewModel.selectedMonth.set(viewModel.selectedDate.get());
        calendarView = rootView.findViewById(R.id.compactcalendar_view);
        calendarView.setListener(myCalendarViewListener);
        calendarView.setCurrentDate(viewModel.selectedDate.get());
        updateCalendarView(scheduledRecordings);

        // List of events for the selected day.
        RecyclerView recyclerView = rootView.findViewById(R.id.rvRecordings);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewListAdapter(new ScheduledRecordingDiffCallback(), viewModel);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    // Listener for the CompactCalendarView.
    private final CompactCalendarView.CompactCalendarViewListener myCalendarViewListener = new CompactCalendarView.CompactCalendarViewListener() {
        @Override
        public void onDayClick(Date date) {
            viewModel.setSelectedDate(date);
        }

        @Override
        public void onMonthScroll(Date date) {
            viewModel.selectedMonth.set(date);
        }
    };

    private void updateCalendarView(List<ScheduledRecording> scheduledRecordings) {
        calendarView.removeAllEvents();
        for (ScheduledRecording item : scheduledRecordings) {
            Event event = new Event(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.accent), item.getStart(), item);
            calendarView.addEvent(event, false);
        }
        calendarView.postInvalidate(); // refresh the calendar view
    }

    // Click on a scheduled recording.
    private void editScheduledRecording(ScheduledRecording item) {
        Intent intent = ScheduledRecordingDetailsActivity.makeIntent(getActivity(), item.getId());
        startActivity(intent);
    }

    // Long click on a scheduled recording.
    private void deleteScheduledRecording(ScheduledRecording item) {
        // Item delete confirm
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_title_delete));
        builder.setMessage(R.string.dialog_text_delete_generic);
        builder.setPositiveButton(R.string.dialog_action_ok,
                (dialogInterface, i) -> viewModel.deleteScheduledRecording(item));
        builder.setCancelable(true);
        builder.setNegativeButton(getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

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
            startActivity(ScheduledRecordingDetailsActivity.makeIntent(getActivity(), Objects.requireNonNull(viewModel.selectedDate.get()).getTime()));
        else
            Toast.makeText(getActivity(), getString(R.string.toast_permissions_denied), Toast.LENGTH_LONG).show();
    }

    private void startScheduledRecordingDetailsActivity() {
        Intent intent = ScheduledRecordingDetailsActivity.makeIntent(getActivity(), Objects.requireNonNull(viewModel.selectedDate.get()).getTime());
        startActivity(intent);
    }

    @VisibleForTesting
    public void clickOnDay(Date date) {
        getActivity().runOnUiThread(() -> myCalendarViewListener.onDayClick(date));
    }
}
