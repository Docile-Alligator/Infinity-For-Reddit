package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

public class ClickableMotionLayout extends MotionLayout {
    private long mStartTime = 0;

    public ClickableMotionLayout(@NonNull Context context) {
        super(context);
    }

    public ClickableMotionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableMotionLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
            mStartTime = event.getEventTime();
        } else if ( event.getAction() == MotionEvent.ACTION_UP ) {
            if ( event.getEventTime() - mStartTime <= ViewConfiguration.getTapTimeout() ) {
                return false;
            }
        }

        return super.onInterceptTouchEvent(event);
    }
}
