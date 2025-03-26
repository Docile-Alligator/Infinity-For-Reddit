package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import ml.docilealligator.infinityforreddit.customviews.slidr.widget.SliderPanel;

public class TableHorizontalScrollView extends HorizontalScrollView {
    @Nullable
    private CustomToroContainer toroContainer;
    @Nullable
    private ViewPager2 viewPager2;
    @Nullable
    private SliderPanel sliderPanel;

    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private boolean allowScroll;
    private boolean isViewPager2Enabled;
    private int touchSlop;

    public TableHorizontalScrollView(Context context) {
        super(context);
        init(context);
    }

    public TableHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public TableHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ViewParent parent = getParent();
            while (parent != null) {
                if (parent instanceof CustomToroContainer) {
                    toroContainer = (CustomToroContainer) parent;
                } else if (parent instanceof ViewPager2) {
                    viewPager2 = (ViewPager2) parent;
                    isViewPager2Enabled = viewPager2.isUserInputEnabled();
                } else if (parent instanceof SliderPanel) {
                    sliderPanel = (SliderPanel) parent;
                }

                parent = parent.getParent();
            }

            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        processMotionEvent(ev);
        return allowScroll || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        processMotionEvent(ev);
        return super.onTouchEvent(ev);
    }

    private void processMotionEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();

                if (toroContainer != null) {
                    toroContainer.requestDisallowInterceptTouchEvent(true);
                }
                if (viewPager2 != null && isViewPager2Enabled) {
                    viewPager2.setUserInputEnabled(false);
                }
                if (sliderPanel != null) {
                    sliderPanel.lock();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                allowScroll = false;

                if (toroContainer != null) {
                    toroContainer.requestDisallowInterceptTouchEvent(false);
                }
                if (viewPager2 != null && isViewPager2Enabled) {
                    viewPager2.setUserInputEnabled(true);
                }
                if (sliderPanel != null) {
                    sliderPanel.unlock();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = ev.getX();
                float currentY = ev.getY();
                float dx = Math.abs(currentX - lastX);
                float dy = Math.abs(currentY - lastY);

                allowScroll = dy < dx && dy > touchSlop && dx > touchSlop;

                if (toroContainer != null) {
                    toroContainer.requestDisallowInterceptTouchEvent(allowScroll);
                }
                if (viewPager2 != null && isViewPager2Enabled) {
                    viewPager2.setUserInputEnabled(false);
                }
                if (sliderPanel != null) {
                    sliderPanel.lock();
                }
                break;
        }
    }
}
