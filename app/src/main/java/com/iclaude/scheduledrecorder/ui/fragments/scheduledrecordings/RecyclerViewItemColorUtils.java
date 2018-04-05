package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.graphics.Color;

/**
 * Utility class used to generate different colors for each element of the RecyclerView.
 */
class RecyclerViewItemColorUtils {
    private static final int[] colors = {Color.argb(255, 255, 193, 7),
            Color.argb(255, 244, 67, 54), Color.argb(255, 99, 233, 112),
            Color.argb(255, 7, 168, 255), Color.argb(255, 255, 7, 251),
            Color.argb(255, 255, 61, 7), Color.argb(255, 205, 7, 255)};


    public static int getColor(int position) {
        int posCol = position % (colors.length);
        return colors[posCol];
    }
}
