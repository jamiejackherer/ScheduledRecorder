/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.fragments.fileviewer;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.Recording;

import java.util.ArrayList;

/**
 * Fragment displaying the list of existing recordings.
 */
public class FileViewerFragment extends Fragment{
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private static final String ARG_POSITION = "position";

    private int position;
    private FileViewerAdapter adapter;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);

        FileViewerViewModel viewModel = ViewModelProviders.of(this).get(FileViewerViewModel.class);
        viewModel.getRecordings().observe(this, recordings -> {
            Log.d(TAG, "recordings list reloaded from database");
            adapter.setRecordings(recordings);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        adapter = new FileViewerAdapter(new ArrayList<Recording>(), getActivity());
        recyclerView.setAdapter(adapter);

        return v;
    }


}




