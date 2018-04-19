package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.databinding.FragmentScheduledRecordingsItemBinding;


/**
 * Adapter for RecyclerView using data binding.
 */
class RecyclerViewListAdapter extends ListAdapter<ScheduledRecording, RecyclerViewViewHolder> {

    private final ScheduledRecordingsViewModel viewModel;

    public RecyclerViewListAdapter(@NonNull DiffUtil.ItemCallback<ScheduledRecording> diffCallback, ScheduledRecordingsViewModel viewModel) {
        super(diffCallback);
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FragmentScheduledRecordingsItemBinding binding = FragmentScheduledRecordingsItemBinding.inflate(layoutInflater, parent, false);

        ScheduledRecordingItemUserActionListener listener = new ScheduledRecordingItemUserActionListener() {
            @Override
            public void onClick(ScheduledRecording scheduledRecording) {
                viewModel.editScheduledRecording(scheduledRecording);
            }

            @Override
            public boolean onLongClick(ScheduledRecording scheduledRecording) {
                return true;
            }
        };
        binding.setListener(listener);

        return new RecyclerViewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    public ScheduledRecording getItemFromPosition(int position) {
        return getItem(position);
    }
}
