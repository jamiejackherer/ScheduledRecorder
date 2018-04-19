/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * When the device is rebooted alarms set with the AlarmManager are cancelled.
 * So we need to use a BroadcastReceiver that gets triggered at bootup in order to start
 * the ScheduledRecordingService and set the next alarm.
 */
public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(ScheduledRecordingService.makeIntent(context));
        else
            context.startService(ScheduledRecordingService.makeIntent(context));
    }
}
