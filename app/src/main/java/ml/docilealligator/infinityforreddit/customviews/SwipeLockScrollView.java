package ml.docilealligator.infinityforreddit.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;

/** {@link HorizontalScrollView} that listens for touch events and locks swipes
 * if it can be scrolled to the right. {@link SwipeLockInterface} must be set for
 * locking to work.
 */
public class SwipeLockScrollView extends HorizontalScrollView implements SwipeLockView {
    @Nullable
    private SwipeLockInterface swipeLockInterface = null;
    private boolean locked = false;

    public SwipeLockScrollView(Context context) {
        super(context);
    }

    public SwipeLockScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeLockScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSwipeLockInterface(@Nullable SwipeLockInterface swipeLockInterface) {
        this.swipeLockInterface = swipeLockInterface;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        updateSwipeLock(ev);
        return super.onInterceptTouchEvent(ev);
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
        }
    }
}
