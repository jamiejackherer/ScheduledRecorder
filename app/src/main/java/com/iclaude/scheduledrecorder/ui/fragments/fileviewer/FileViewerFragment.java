/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.ui.fragments.PlaybackFragment;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import static com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface.OperationResult;

/**
 * Fragment displaying the list of existing recordings.
 */
public class FileViewerFragment extends Fragment{
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String ARG_POSITION = "position";

    private FileViewerViewModel viewModel;
    private AdapterDataBinding adapter;
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

        viewModel = ViewModelProviders.of(this).get(FileViewerViewModel.class);
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

        adapter = new AdapterDataBinding(context, new ArrayList<>(), clickCallback);
        recyclerView.setAdapter(adapter);

        return v;
    }

    // Click listeners for the elements of the list.
    private final RecordingsClickCallback clickCallback = new RecordingsClickCallback() {
        @Override
        public void onClick(Recording recording) {
            playRecording(recording);
        }

        @Override
        public boolean onLongClick(Recording recording) {
            showDialogLongClick(recording);
            return true;
        }
    };

    // Play.
    private void playRecording(Recording recording) {
        try {
            PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recording);

            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getFragmentManager()
                    .beginTransaction();

            playbackFragment.show(transaction, "dialog_playback");

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
            if (item == 0) {
                shareFile(recording);
            }
            if (item == 1) {
                renameFileDialog(recording);
            } else if (item == 2) {
                deleteFileDialog(recording);
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Share.
    private void shareFile(Recording recording) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(recording.getPath())));
        shareIntent.setType("audio/mp4");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.send_to)));
    }

    // Rename.
    private void renameFileDialog(Recording recording) {
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
                    rename(recording, newName);

                    dialog.cancel();
                });
        renameFileBuilder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    private void rename(Recording recording, String newName) {
        OperationResult callback = new OperationResult() {
            @Override
            public void onSuccess() {
                Log.i(TAG, CLASS_NAME + ": recording renamed");
            }

            @Override
            public void onFailure() {
                Log.e(TAG, CLASS_NAME + ": error while renaming file in database");
            }
        };

        String newPath = Utils.getDirectoryPath(context);
        newPath += "/" + newName;
        File f = new File(newPath);

        if (f.exists() && !f.isDirectory()) {
            Toast.makeText(context, String.format(context.getString(R.string.toast_file_exists), newName), Toast.LENGTH_SHORT).show();
        } else {
            File oldFilePath = new File(recording.getPath());
            boolean renamed = oldFilePath.renameTo(f);
            if (!renamed) {
                Toast.makeText(context, context.getString(R.string.toast_file_rename_error), Toast.LENGTH_SHORT).show();
                return;
            }

            recording.setName(newName);
            recording.setPath(newPath);
            viewModel.renameRecording(recording, callback);
        }
    }

    // Delete.
    private void deleteFileDialog(Recording recording) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
        confirmDelete.setTitle(context.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(context.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(context.getString(R.string.dialog_action_yes),
                (dialog, id) -> {
                    deleteFile(recording);

                    dialog.cancel();
                });
        confirmDelete.setNegativeButton(context.getString(R.string.dialog_action_no),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    private void deleteFile(Recording recording) {
        // Delete file from storage.
        File file = new File(recording.getPath());
        boolean deleted = file.delete();
        String msg = deleted ? context.getString(R.string.toast_file_delete, recording.getName()) : context.getString(R.string.toast_file_delete_error);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        if (!deleted) return;

        viewModel.deleteRecording(recording, new OperationResult() {
            @Override
            public void onSuccess() {
                Log.i(TAG, CLASS_NAME + ": recording deleted from database");
            }

            @Override
            public void onFailure() {
                Log.e(TAG, CLASS_NAME + ": error deleting recording from database");

            }
        });
    }
}




