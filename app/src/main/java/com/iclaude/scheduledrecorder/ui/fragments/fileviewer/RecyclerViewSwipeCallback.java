package com.iclaude.scheduledrecorder.ui.fragments.fileviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.utils.Utils;

import java.util.Objects;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

/**
 * Manages swipes on the items of the list and the actions: rename, share and delete.
 */
public class RecyclerViewSwipeCallback extends Callback {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private static final int BUTTON_RIGHT_WIDTH_DP = 50;
    private static final int BUTTON_LEFT_WIDTH_DP = 100;

    private boolean swipeBack = false;
    private ButtonsState buttonShowedState = ButtonsState.GONE;
    private RectF buttonInstance = null;
    private RectF buttonEdit = null;
    private RectF buttonShare = null;
    private RectF buttonDelete = null;
    private RecyclerView.ViewHolder currentItemViewHolder = null;
    private final SwipeControllerActions buttonsActions;
    private final int backgroundColor;
    private final Drawable deleteIcon;
    private final Drawable editIcon;
    private final Drawable shareIcon;
    private final float buttonRightWidth, buttonLeftWidth;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final int cardViewCorner;
    private final int cardViewMargin;

    // Variables used to restore the layout when clicking on the items.
    private Canvas c;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;
    private float dY;
    private int actionState;
    private boolean isCurrentlyActive;


    enum ButtonsState {
        GONE,
        LEFT_VISIBLE,
        RIGHT_VISIBLE
    }


    public RecyclerViewSwipeCallback(SwipeControllerActions buttonsActions, Context context) {
        this.buttonsActions = buttonsActions;

        backgroundColor = ContextCompat.getColor(context, R.color.primary_light);
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
        editIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit_white_24dp);
        shareIcon = ContextCompat.getDrawable(context, R.drawable.ic_share_white_24dp);
        intrinsicWidth = Objects.requireNonNull(deleteIcon).getIntrinsicWidth();
        intrinsicHeight = deleteIcon.getIntrinsicHeight();
        cardViewCorner = context.getResources().getDimensionPixelSize(R.dimen.cardview_cornerRadius);
        cardViewMargin = context.getResources().getDimensionPixelSize(R.dimen.cardview_margin);
        buttonLeftWidth = Utils.convertDpToPixel(context, BUTTON_LEFT_WIDTH_DP);
        buttonRightWidth = Utils.convertDpToPixel(context, BUTTON_RIGHT_WIDTH_DP);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = buttonShowedState != ButtonsState.GONE;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState != ButtonsState.GONE) {
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, buttonLeftWidth);
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX, -buttonRightWidth);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if (buttonShowedState == ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        currentItemViewHolder = viewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) ->
                touch(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive, event));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) ->
                touchDown(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive, event));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) ->
                touchUp(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive, event));
    }

    @SuppressWarnings("SameReturnValue")
    private boolean touch(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive, MotionEvent event) {
        swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
        if (swipeBack) {
            if (dX < -buttonRightWidth) buttonShowedState = ButtonsState.RIGHT_VISIBLE;
            else if (dX > buttonLeftWidth) buttonShowedState  = ButtonsState.LEFT_VISIBLE;

            if (buttonShowedState != ButtonsState.GONE) {
                saveSwipedView(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive);

                setTouchDownListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive);
                setItemsClickable(recyclerView, false);
            }
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean touchDown(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setTouchUpListener(c, recyclerView, viewHolder, dY, actionState, isCurrentlyActive);
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    @SuppressLint("ClickableViewAccessibility")
    private boolean touchUp(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dY, final int actionState, final boolean isCurrentlyActive, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            RecyclerViewSwipeCallback.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
            recyclerView.setOnTouchListener((v1, event1) -> false);
            setItemsClickable(recyclerView, true);
            swipeBack = false;

            if (buttonsActions != null && buttonInstance != null && buttonInstance.contains(event.getX(), event.getY())) {
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                    if(buttonEdit.contains(event.getX(), event.getY()))
                        buttonsActions.renameFile(viewHolder.getAdapterPosition());
                    else if(buttonShare.contains(event.getX(), event.getY()))
                        buttonsActions.shareFile(viewHolder.getAdapterPosition());
                }
                else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                    if(buttonDelete.contains(event.getX(), event.getY()))
                        buttonsActions.deleteFile(viewHolder.getAdapterPosition());
                }
            }
            buttonShowedState = ButtonsState.GONE;
            currentItemViewHolder = null;
        }
        return false;
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        Paint p = new Paint();
        p.setColor(backgroundColor);

        // Draw the background color for left and right buttons.
        RectF leftButton = new RectF(itemView.getLeft() + cardViewMargin, itemView.getTop() + cardViewMargin, itemView.getLeft() + buttonLeftWidth, itemView.getBottom() - cardViewMargin);
        c.drawRoundRect(leftButton, cardViewCorner, cardViewCorner, p);
        RectF rightButton = new RectF(itemView.getRight() - buttonRightWidth, itemView.getTop() + cardViewMargin, itemView.getRight() - cardViewMargin, itemView.getBottom() - cardViewMargin);
        c.drawRoundRect(rightButton, cardViewCorner, cardViewCorner, p);

        // Draw delete icon.
        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int iconLeft = (int) (itemView.getRight() - buttonRightWidth + buttonRightWidth / 2 - intrinsicWidth/2 - cardViewMargin/2);
        int iconRight = (int) (itemView.getRight() - buttonRightWidth + buttonRightWidth / 2 + intrinsicWidth/2 - cardViewMargin/2);
        int iconBottom = iconTop + intrinsicHeight;
        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        deleteIcon.draw(c);
        // Get specific RectF for edit and delete button.
        buttonDelete = new RectF(iconLeft, iconTop, iconRight, iconBottom);

        // Draw edit icon.
        iconLeft = (int) ((buttonLeftWidth / 4 - intrinsicWidth / 2) + cardViewMargin) + itemView.getLeft();
        iconRight = (int) ((buttonLeftWidth / 4 + intrinsicWidth / 2) + cardViewMargin) + itemView.getLeft();
        editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        editIcon.draw(c);
        // Get specific RectF for edit button.
        buttonEdit = new RectF(iconLeft, iconTop, iconRight, iconBottom);

        // Draw share icon.
        iconLeft = (int) ((buttonLeftWidth * 3/4 - intrinsicWidth / 2) - cardViewMargin) + + itemView.getLeft();
        iconRight = (int) ((buttonLeftWidth * 3/4 + intrinsicWidth / 2) - cardViewMargin) + + itemView.getLeft();
        shareIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        shareIcon.draw(c);
        // Get specific RectF for share button.
        buttonShare = new RectF(iconLeft, iconTop, iconRight, iconBottom);

        buttonInstance = null;
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton;
        }
        else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton;
        }
    }

    public void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder);
        }
    }

    /**
     * When the buttons are visible and the user clicks an item, we want to restore
     * the layout to its initial state (buttons not visible).
     * These methods are called from the click listeners set in the adapter of
     * the RecyclerView (RecyclerViewListAdapter).
     */
    public void restoreLayout() {
        RecyclerViewSwipeCallback.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
        //recyclerView.setOnTouchListener((v1, event1) -> false);
        setItemsClickable(recyclerView, true);
        swipeBack = false;
        buttonShowedState = ButtonsState.GONE;
        currentItemViewHolder = null;
    }

    public boolean buttonsAreVisible() {
        return buttonShowedState != ButtonsState.GONE;
    }

    // Save the data of the swiped view to restore the layout when the user clicks on an item.
    private void saveSwipedView(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dY, int actionState, boolean isCurrentlyActive) {
        this.c = c;
        this.recyclerView = recyclerView;
        this.viewHolder = viewHolder;
        this.dY = dY;
        this.actionState = actionState;
        this.isCurrentlyActive = isCurrentlyActive;
    }
}
