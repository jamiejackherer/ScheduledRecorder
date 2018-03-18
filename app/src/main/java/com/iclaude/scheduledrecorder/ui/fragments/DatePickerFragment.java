/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Shows a dialog to pick a date.
 * Communicates the date selected through an interface.
 * This class stores and communicates the id of the view that needs the date, so it can be used
 * for different views within the same Activity/Fragment.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String EXTRA_VIEW_ID = "com.iclaude.scheduledrecorder.EXTRA_VIEW_ID";
    private static final String EXTRA_DATE = "com.iclaude.scheduledrecorder.EXTRA_DATE";


    private MyOnDateSetListener listener;

    public static DatePickerFragment newInstance(long viewId, long date) {
        DatePickerFragment f = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_VIEW_ID, viewId);
        bundle.putLong(EXTRA_DATE, date);
        f.setArguments(bundle);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long date = getArguments().getLong(EXTRA_DATE);
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (MyOnDateSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DatePickerFragment.MyOnDateSetListener");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (MyOnDateSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement DatePickerFragment.MyOnDateSetListener");
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (listener != null) {
            listener.onDateSet(getArguments().getLong(EXTRA_VIEW_ID, 0), year, month, day);
        }
    }

    // Interface form communication with the Activity.
    public interface MyOnDateSetListener {
        void onDateSet(long viewId, int year, int month, int day);
    }
}
