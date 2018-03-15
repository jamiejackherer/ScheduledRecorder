package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import javax.inject.Inject;

/**
 * ViewModel for each recording of the list.
 */

public class RecordingViewModel {
    private static final String TAG = "SCHEDULED_RECORDER_TAG";
    private final String CLASS_NAME = getClass().getSimpleName();

    private final Recording recording;
    @Inject
    RecordingsRepository recordingsRepository;


    public RecordingViewModel(Recording recording) {
        App.getComponent().inject(this);

        this.recording = recording;
    }

    public String getName() {
        return recording.getName();
    }

    public long getTimeAdded() {
        return recording.getTimeAdded();
    }

    public long getLength() {
        return recording.getLength();
    }

    // Click and long click listeners.
    public void onClick(Context context) {
        playRecording(context);
    }

    public boolean onLongClick(Context context) {
        showDialogLongClick(context);
        return true;
    }

    // Play.
    private void playRecording(Context context) {
        try {
            PlaybackFragment playbackFragment = new PlaybackFragment().newInstance(recording);
            playbackFragment.show(((Activity) context).getFragmentManager(), "dialog_playback");
        } catch (Exception e) {
            Log.e(TAG, CLASS_NAME + ": error in playing the recording" + e.toString());
        }
    }

    // Long click options.
    private void showDialogLongClick(Context context) {
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
                shareFile(context);
            }
            if (item == 1) {
                renameFileDialog(context);
            } else if (item == 2) {
                deleteFileDialog(context);
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Share.
    private void shareFile(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(recording.getPath())));
        shareIntent.setType("audio/mp4");
        context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.send_to)));
    }

    // Rename.
    private void renameFileDialog(Context context) {
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
                    rename(context, newName);

                    dialog.cancel();
                });
        renameFileBuilder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                (dialog, id) -> dialog.cancel());

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    private void rename(Context context, String newName) {
        RecordingsRepositoryInterface.OperationResult callback = new RecordingsRepositoryInterface.OperationResult() {
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
            recordingsRepository.updateRecordings(callback, recording);
        }
    }

    // Delete.
    private void deleteFileDialog(Context context) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(context);
        confirmDelete.setTitle(context.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(context.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(context.getString(R.string.dialog_action_yes),
                (dialog, id) -> {
                    deleteFile(context);

                    dialog.cancel();
                });
        confirmDelete.setNegativeButton(context.getString(R.string.dialog_action_no),
                (dialog, id) -> dialog.cancel());

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    private void deleteFile(Context context) {
        // Delete file from storage.
        File file = new File(recording.getPath());
        boolean deleted = file.delete();
        String msg = deleted ? context.getString(R.string.toast_file_delete, recording.getName()) : context.getString(R.string.toast_file_delete_error);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        if (!deleted) return;

        recordingsRepository.deleteRecordings(new RecordingsRepositoryInterface.OperationResult() {
            @Override
            public void onSuccess() {
                Log.i(TAG, CLASS_NAME + ": recording deleted from database");
            }

            @Override
            public void onFailure() {
                Log.e(TAG, CLASS_NAME + ": error deleting recording from database");

            }
        }, recording);
    }
}
