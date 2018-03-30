/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.didagger2;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;

import com.iclaude.scheduledrecorder.database.AppDatabase;
import com.iclaude.scheduledrecorder.database.RecordingsDao;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.utils.AppExecutors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides all the objects necessary to interact with the database, with all the relevant
 * dependencies.
 */

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    @NonNull
    public AppExecutors provideAppExecutors() {
        return new AppExecutors();
    }

    @Provides
    @Singleton
    @NonNull
    public AppDatabase provideAppDatabase(Context context) {
        return Room.databaseBuilder(context,
                AppDatabase.class, "Recordings.db")
                .build();
    }

    @Provides
    @Singleton
    @NonNull
    public RecordingsDao provideRecordingsDao(AppDatabase appDatabase) {
        return appDatabase.recordingsDao();
    }

    @Provides
    @Singleton
    @NonNull
    public RecordingsRepository provideRecordingsRepository(AppExecutors appExecutors, RecordingsDao recordingsDao) {
        return new RecordingsRepository();
    }
}
