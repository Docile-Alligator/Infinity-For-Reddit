package ml.docilealligator.infinityforreddit.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeLockLinearLayout extends LinearLayout implements SwipeLockView {
    @Nullable
    private SwipeLockInterface swipeLockInterface = null;
    private boolean locked = false;

    public SwipeLockLinearLayout(@NonNull Context context) {
        super(context);
    }

    public SwipeLockLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeLockLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeLockLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setSwipeLockInterface(@Nullable SwipeLockInterface swipeLockInterface) {
        this.swipeLockInterface = swipeLockInterface;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        updateSwipeLock(ev);
        return locked;
    }

    @SuppressLint("ClickableViewAccessibility") // we are just listening to touch events
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        updateSwipeLock(ev);
        return super.onTouchEvent(ev);
    }

    /**
     * Unlocks swipe if the view cannot be scrolled right anymore or if {@code ev} is
     * {@link MotionEvent#ACTION_UP} or {@link MotionEvent#ACTION_CANCEL}
     */
    private void updateSwipeLock(MotionEvent ev) {
        if (swipeLockInterface != null) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_CANCEL) {
                // calling SlidrInterface#unlock aborts the swipe
                // so don't call unlock if it is already unlocked
                if (locked) {
                    swipeLockInterface.unlockSwipe();
                    locked = false;
                }
            } else {
                if (!locked) {
                    swipeLockInterface.lockSwipe();
                    locked = true;
                }
            }

            swipeLockInterface.setSwipeLocked(locked);
        }
    }
}
