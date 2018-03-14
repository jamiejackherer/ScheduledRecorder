/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import com.iclaude.scheduledrecorder.database.Recording;

/**
 * Interface managing the clicks and long-clicks on the list of recordings.
 */

public interface RecordingsClickCallback {
    void onClick(Recording recording);

    boolean onLongClick(Recording recording);
}
