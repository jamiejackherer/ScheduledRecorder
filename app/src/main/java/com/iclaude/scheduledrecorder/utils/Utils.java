/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Common utility methods.
 */

public class Utils {

    /*
        Get, or create if necessary, the path of the directory where to save recordings.
     */
    public static String getDirectoryPath(Context context) {
        String directoryPath;

        if (isExternalStorageWritable()) {
            directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder";
            File f = new File(directoryPath);
            boolean available = f.mkdirs() || f.isDirectory();
            if (available)
                return directoryPath;
        }

        return context.getFilesDir().getAbsolutePath();
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    // Format date (date, time in short format).
    public static String formatDateTimeShort(long time) {
        Date timeAdded = new Date(time);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return df.format(timeAdded);
    }

    // Format date in medium format.
    public static String formatDateMedium(long time) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateFormat.format(new Date(time));
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
        return cal.get(Calendar.HOUR);
    }

    public static int getMinuteFromTimeMillis(long timeMillis) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeMillis);
        return cal.get(Calendar.MINUTE);
    }
}
