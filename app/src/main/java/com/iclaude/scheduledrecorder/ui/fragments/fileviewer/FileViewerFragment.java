/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.R;

import java.util.ArrayList;

/**
 * Fragment displaying the list of existing recordings.
 */
public class FileViewerFragment extends Fragment{
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String ARG_POSITION = "position";

    private RecyclerViewAdapter adapter;
    private Context context;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileViewerViewModel viewModel = ViewModelProviders.of(this).get(FileViewerViewModel.class);
        viewModel.getRecordings().observe(this, recordings -> {
            Log.d(TAG, "recordings list reloaded from database");
            adapter.setRecordings(recordings);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        adapter = new RecyclerViewAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return v;
    }
}




