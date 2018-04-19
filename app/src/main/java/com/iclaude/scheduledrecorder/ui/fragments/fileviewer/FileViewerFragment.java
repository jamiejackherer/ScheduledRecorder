/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerBinding;
import com.iclaude.scheduledrecorder.ui.fragments.PlaybackFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


/**
 * Fragment displaying the list of existing recordings.
 */
public class FileViewerFragment extends Fragment implements RecordingUserActions {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String ARG_POSITION = "position";

    private FileViewerViewModel viewModel;
    private RecyclerViewListAdapter adapter;
    private Context context;
    private RecyclerViewSwipeCallback swipeController;

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

        viewModel = ViewModelProviders.of(this).get(FileViewerViewModel.class);

        viewModel.getRecordings().observe(this, recordings -> adapter.submitList(recordings));

        viewModel.getPlayRecordingEvent().observe(this, this::playRecording);

        viewModel.getLongClickItemEvent().observe(this, this::showDialogLongClick);

        viewModel.getDeleteCommand().observe(this, msgId -> Toast.makeText(context, getString(msgId), Toast.LENGTH_SHORT).show());

        viewModel.getUpdateCommand().observe(this, msgId -> Toast.makeText(context, getString(msgId), Toast.LENGTH_SHORT).show());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentFileViewerBinding binding = FragmentFileViewerBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        View rootView = binding.getRoot();

        // List of recordings.

        // RecyclerView setup.
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(context, context.getResources().getInteger(R.integer.fileviewer_num_columns));
        recyclerView.setLayoutManager(layoutManager);

        // Swipe controller.
        swipeController = new RecyclerViewSwipeCallback(new SwipeControllerActions() {
            @Override
            public void shareFile(int position) {
                FileViewerFragment.this.shareFile(adapter.getRecordingFromPosition(position));
            }

            @Override
            public void renameFile(int position) {
                FileViewerFragment.this.renameFile(adapter.getRecordingFromPosition(position));
            }

            @Override
            public void deleteFile(int position) {
                FileViewerFragment.this.deleteFile(adapter.getRecordingFromPosition(position));
            }
        }, getActivity());
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        // Adapter.
        adapter = new RecyclerViewListAdapter(new RecordingDiffCallback(), viewModel, swipeController);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    // Play.
    @Override
    public void playRecording(Recording recording) {
        try {
            PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recording);
            playbackFragment.show((Objects.requireNonNull(getActivity()).getFragmentManager()), "dialog_playback");
        } catch (Exception e) {
            Log.e(TAG, CLASS_NAME + ": error in playing the recording" + e.toString());
        }
    }

    // Long click options.
    private void showDialogLongClick(Recording recording) {
        ArrayList<String> entries = new ArrayList<>();
        entries.add(context.getString(R.string.dialog_file_share));
        entries.add(context.getString(R.string.dialog_file_rename));
        entries.add(context.getString(R.string.dialog_file_delete));

        final CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);

        // File delete confirm
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_title_options));
        builder.setItems(items, (dialog, item) -> {
            switch (item) {
                case 0:
                    shareFile(recording);
                    break;
                case 1:
                    renameFile(recording);
                    break;
                case 2:
                    deleteFile(recording);
                    break;
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Share.
    @Override
    public void shareFile(Recording recording) {
        Uri fileUri = FileProvider.getUriForFile(getActivity(), "com.iclaude.scheduledrecorder.provider",
                new File(recording.getPath()));

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType("audio/mp4");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.send_to)));
    }

    // Rename.
    @Override
    public void renameFile(Recording recording) {
        // File rename dialog.
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(context.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(context.getString(R.string.dialog_action_ok),
                (dialog, id) -> {
                    String newName = input.getText().toString().trim();
                    viewModel.updateRecording(recording, newName);

                    dialog.cancel();
                });
        renameFileBuilder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    // Delete.
    @Override
    public void deleteFile(Recording recording) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
        confirmDelete.setTitle(context.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(context.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(context.getString(R.string.dialog_action_yes),
                (dialog, id) -> {
                    viewModel.deleteRecording(recording);

                    dialog.cancel();
                });
        confirmDelete.setNegativeButton(context.getString(R.string.dialog_action_no),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }
}




