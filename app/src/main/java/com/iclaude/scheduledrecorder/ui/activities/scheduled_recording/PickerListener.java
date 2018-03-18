/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.activities.scheduled_recording;

import android.view.View;

/**
 * Interface for showing a picker dialog to select a date or time.
 */

public interface PickerListener {
    void showDatePickerDialog(View view);

    void showTimePickerDialog(View view);
}
