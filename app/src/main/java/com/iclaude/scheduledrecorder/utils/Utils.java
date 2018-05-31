/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Common utility methods.
 */

public class Utils {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    /*
        Get, or create if necessary, the path of the directory where to save recordings.
     */
    public static String getDirectoryPath(Context context) {
        if (isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS), "ScheduledRecorder");
            if (file.mkdirs()) {
                return file.getAbsolutePath();
            }
        }

        return context.getFilesDir().getAbsolutePath(); // use internal storage if external storage is not available
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Format date and time in short format.
    public static String formatDateTimeShort(long time) {
        Date timeAdded = new Date(time);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(timeAdded);
    }

    // Format date in short format.
    public static String formatDateShort(long time) {
        Date timeAdded = new Date(time);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return df.format(timeAdded);
    }

    // Format date in medium format.
    public static String formatDateMedium(long time) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateFormat.format(new Date(time));
    }

    // Format date in "Sunday 21" like format.
    public static String formatDateDayNumber(Date date) {
        return new SimpleDateFormat("EEEE d", Locale.getDefault()).format(date);
    }

    // Gets the name of the month ("January") from a Date.
    public static String formatDateMonthName(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return dateFormat.format(date);
    }

    // Format time in hh:mm format.
    public static String formatTime(long time) {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        return dateFormat.format(new Date(time));
    }

    // Format duration (hh:mm:ss).
    public static String formatDuration(long duration) {
        String hms;
        if (TimeUnit.MILLISECONDS.toHours(duration) > 0)
            hms = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        else
            hms = String.format(Locale.getDefault(),"%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));

        return hms;
    }

    public static int getHourFromTimeMillis(long timeMillis) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeMillis);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinuteFromTimeMillis(long timeMillis) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeMillis);
        return cal.get(Calendar.MINUTE);
    }

    // Format seconds elapsed for chronometer in mm:ss format.
    public static String formatSecondsElapsedForChronometer(int seconds) {
        SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        return mTimerFormat.format(new Date(seconds * 1000L));
    }

    // Given a Date, returns the long value for the time at 00:00 of that particular day.
    public static long getDayStartTimeLong(Date date) {
        Calendar calCmd = new GregorianCalendar();
        calCmd.setTimeInMillis(date.getTime());
        calCmd.set(Calendar.HOUR_OF_DAY, 0);
        calCmd.set(Calendar.MINUTE, 0);
        calCmd.set(Calendar.SECOND, 0);
        calCmd.set(Calendar.MILLISECOND, 0);
        return calCmd.getTimeInMillis();
    }

    // Given a Date, returns the long value for the time at 00:00 of the next day.
    public static long getDayEndTimeLong(Date date) {
        Calendar calCmd = new GregorianCalendar();
        calCmd.setTimeInMillis(date.getTime());
        calCmd.set(Calendar.HOUR_OF_DAY, 23);
        calCmd.set(Calendar.MINUTE, 59);
        calCmd.set(Calendar.SECOND, 59);
        calCmd.set(Calendar.MILLISECOND, 999);
        return calCmd.getTimeInMillis();
    }

    public static int convertDpToPixel(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
