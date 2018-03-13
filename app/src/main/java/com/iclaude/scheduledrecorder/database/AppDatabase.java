/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(entities = {Recording.class, ScheduledRecording.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordingsDao recordingsDao();
}
