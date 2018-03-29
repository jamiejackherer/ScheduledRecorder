package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.database.Recording;
import com.iclaude.scheduledrecorder.databinding.FragmentFileViewerItemBinding;

/**
 * Adapter for RecyclerView using data binding.
 */
public class RecyclerViewListAdapter extends ListAdapter<Recording, RecyclerViewViewHolder> {

    private final FileViewerViewModel fileViewerViewModel;

    public RecyclerViewListAdapter(@NonNull DiffUtil.ItemCallback<Recording> diffCallback, FileViewerViewModel fileViewerViewModel) {
        super(diffCallback);
        this.fileViewerViewModel = fileViewerViewModel;
    }

    @NonNull
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentFileViewerItemBinding binding = FragmentFileViewerItemBinding.inflate(layoutInflater, parent, false);

        RecordingItemUserActionListener listener = new RecordingItemUserActionListener() {
            @Override
            public void onClick(Recording recording) {
                fileViewerViewModel.playRecording(recording);
            }

            @Override
            public boolean onLongClick(Recording recording) {
                fileViewerViewModel.showLongClickDialogOptions(recording);
                return true;
            }
        };
        binding.setListener(listener);

        return new RecyclerViewViewHolder(binding);
    }
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}
