package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.graphics.Color;
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
    private final int color1 = Color.argb(255, 186, 104, 200);
    private final int color2 = Color.argb(255, 105, 240, 174);


    public RecyclerViewViewHolder(FragmentScheduledRecordingsItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        tvColor = binding.getRoot().findViewById(R.id.tvColor);
    }

    public void bind(ScheduledRecording scheduledRecording, int position) {
        binding.setScheduledRecording(scheduledRecording);
        binding.executePendingBindings();

        if(position % 2 == 0)
            tvColor.setBackgroundColor(color1);
        else
            tvColor.setBackgroundColor(color2);
    }
}
