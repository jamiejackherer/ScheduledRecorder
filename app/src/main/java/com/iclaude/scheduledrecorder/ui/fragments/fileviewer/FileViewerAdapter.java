/*
 * Copyright (c) 2018 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
 */

package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.database.RecordingsRepositoryInterface;
import com.iclaude.scheduledrecorder.didagger2.App;
import com.iclaude.scheduledrecorder.ui.fragments.PlaybackFragment;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Adapter for the RecyclerView in the FileViewerFragment.
 */

public class FileViewerAdapter extends RVAdapterDataBinding {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();

    private List<Recording> recordings;
    private final Context context;
    @Inject
    RecordingsRepository recordingsRepository;

    public FileViewerAdapter(List<Recording> recordings, Context context) {
        this.recordings = recordings;
        this.context = context;

        App.getComponent().inject(this);
    }

    @Override
    public RVViewHolderDataBinding onCreateViewHolder(ViewGroup parent, int viewType) {
        RVViewHolderDataBinding viewHolder = super.onCreateViewHolder(parent, viewType);
        viewHolder.getBinding().getRoot().setOnClickListener(view -> playRecording(viewHolder.getAdapterPosition()));

        viewHolder.getBinding().getRoot().setOnLongClickListener(view -> {
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
                    shareFile(viewHolder.getAdapterPosition());
                }
                if (item == 1) {
                    renameFileDialog(viewHolder.getAdapterPosition());
                } else if (item == 2) {
                    deleteFileDialog(viewHolder.getAdapterPosition());
                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                    (dialog, id) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            return false;
        });

        return viewHolder;
    }

    public void setRecordings(List<Recording> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    @Override
    protected Object getObjForPosition(int position) {
        return recordings.get(position);
    }

    @Override
    protected int getLayoutIdForPosition(int position) {
        return R.layout.fragment_file_viewer_item;
    }

    private void playRecording(int pos) {
        try {
            PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recordings.get(pos));

            FragmentTransaction transaction = ((FragmentActivity) context)
                    .getFragmentManager()
                    .beginTransaction();

            playbackFragment.show(transaction, "dialog_playback");

        } catch (Exception e) {
            Log.e(TAG, CLASS_NAME + " - playRecording: exception: " + e.toString());
        }
    }

    private void shareFile(int pos) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(recordings.get(pos).getPath())));
        shareIntent.setType("audio/mp4");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.send_to)));
    }

    private void deleteFileDialog(int pos) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
        confirmDelete.setTitle(context.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(context.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(context.getString(R.string.dialog_action_yes),
                (dialog, id) -> {
                    deleteFile(pos);

                    dialog.cancel();
                });
        confirmDelete.setNegativeButton(context.getString(R.string.dialog_action_no),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    // Remove item from database, RecyclerView and storage.
    private void deleteFile(int pos) {
        Recording recording = recordings.get(pos);

        // Delete file from storage.
        File file = new File(recording.getPath());
        boolean deleted = file.delete();
        int stringId = deleted ? R.string.toast_file_delete : R.string.toast_file_delete_error;
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show();
        if (!deleted) return;

        recordingsRepository.deleteRecordings(new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                Log.d(TAG, CLASS_NAME + " - deleteFile(): recording deleted from database");
            }

            @Override
            public void onFailure() {
                Log.e(TAG, CLASS_NAME + " - deleteFile(): error deleting recording from database");
            }
        }, recording);
    }

    private void renameFileDialog(int pos) {
        // File rename dialog.
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(context.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(context.getString(R.string.dialog_action_ok),
                (dialog, id) -> {
                    String value = input.getText().toString().trim();
                    rename(pos, value);

                    dialog.cancel();
                });
        renameFileBuilder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    private void rename(int pos, String newName) {
        Recording recording = recordings.get(pos);

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
            recordingsRepository.updateRecordings(new RecordingsRepositoryInterface.OperationResult() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure() {
                    Log.e(TAG, CLASS_NAME + " - rename(): error while renaming file in database");
                }
            }, recording);
        }
    }
}
