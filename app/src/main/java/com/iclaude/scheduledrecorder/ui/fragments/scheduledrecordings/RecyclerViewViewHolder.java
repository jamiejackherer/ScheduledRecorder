package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.ScheduledRecording;
import com.iclaude.scheduledrecorder.databinding.FragmentScheduledRecordingsItemBinding;

/**
 * ViewHolder for using data binding in RecyclerViews.
 */
public class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
    private final FragmentScheduledRecordingsItemBinding binding;
    private final TextView tvColor;

    public RecyclerViewViewHolder(FragmentScheduledRecordingsItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        tvColor = binding.getRoot().findViewById(R.id.tvColor);
    }

    public void bind(ScheduledRecording scheduledRecording, int position) {
        binding.setScheduledRecording(scheduledRecording);
        binding.executePendingBindings();
        tvColor.setBackground(new ColorDrawable(RecyclerViewItemColorUtils.getColor(position)));
    }
}
