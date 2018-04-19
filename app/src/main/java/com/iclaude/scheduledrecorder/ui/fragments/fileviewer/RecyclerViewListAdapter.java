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
    private final RecyclerViewSwipeCallback swipeCallback;

    public RecyclerViewListAdapter(@NonNull DiffUtil.ItemCallback<Recording> diffCallback, FileViewerViewModel fileViewerViewModel, RecyclerViewSwipeCallback swipeCallback) {
        super(diffCallback);
        this.fileViewerViewModel = fileViewerViewModel;
        this.swipeCallback = swipeCallback;
    }

    @NonNull
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentFileViewerItemBinding binding = FragmentFileViewerItemBinding.inflate(layoutInflater, parent, false);

        RecordingItemUserActionListener listener = new RecordingItemUserActionListener() {
            @Override
            public void onClick(Recording recording) {
                if(swipeCallback.buttonsAreVisible())
                    swipeCallback.restoreLayout();
                else
                    fileViewerViewModel.playRecording(recording);
            }

            @Override
            public boolean onLongClick(Recording recording) {
                if(swipeCallback.buttonsAreVisible())
                    swipeCallback.restoreLayout();
                //else
                    // actions to perform on long click
                    //fileViewerViewModel.showLongClickDialogOptions(recording);

                return true;
            }
        };
        binding.setListener(listener);

        return new RecyclerViewViewHolder(binding);
    }

    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public Recording getRecordingFromPosition(int position) {
        return getItem(position);
    }
}
