package com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.iclaude.scheduledrecorder.R;

import java.util.Objects;

abstract class RecyclerViewSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final Drawable deleteIcon;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final Drawable background;
    private final int backgroundColor;
    private final Paint clearPaint;

    public RecyclerViewSwipeToDeleteCallback(int dragDirs, int swipeDirs, Context context) {
        super(dragDirs, swipeDirs);

        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
        intrinsicWidth = Objects.requireNonNull(deleteIcon).getIntrinsicWidth();
        intrinsicHeight = deleteIcon.getIntrinsicHeight();
        background = new ColorDrawable();
        backgroundColor = ContextCompat.getColor(context, R.color.primary_light);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        boolean isCanceled = dX == 0f && !isCurrentlyActive;

        if (isCanceled) {
            clearCanvas(c, itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
            return;
        }


        // Draw the delete background.
        ((ColorDrawable)background).setColor(backgroundColor);
        int leftBack = dX < 0 ? (int)(itemView.getRight() + dX) : 0;
        int rightBack = dX < 0 ? itemView.getRight() : (int) dX;
        background.setBounds(
                leftBack,
                itemView.getTop(),
                rightBack,
                itemView.getBottom()
        );
        background.draw(c);

        // Calculate position of delete icon.
        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int iconMargin = (itemHeight - intrinsicHeight) / 2;
        int iconBottom = iconTop + intrinsicHeight;
        int iconLeft, iconRight;
        if(dX < 0) {
            iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
            iconRight = itemView.getRight() - iconMargin;
        } else {
            iconLeft = iconMargin;
            iconRight = iconMargin + intrinsicWidth;
        }

        // Draw the delete icon
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        deleteIcon.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas canvas, float left, float top, float right, float bottom) {

        canvas.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }
}
