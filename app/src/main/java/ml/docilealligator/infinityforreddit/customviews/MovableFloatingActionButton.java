package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class MovableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {
    private final static float CLICK_DRAG_TOLERANCE = 50;
    private long downTime = 0;
    private boolean moved = false;
    private boolean longClicked = false;

    private float downRawX, downRawY;
    private float dX, dY;

    @Nullable
    private Display display;
    @Nullable
    private SharedPreferences postDetailsSharedPreferences;
    private boolean portrait;

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downTime = System.currentTimeMillis();
            moved = false;

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!moved) {
                if (System.currentTimeMillis() - downTime >= 300) {
                    if (!longClicked) {
                        longClicked = true;
                        return performLongClick();
                    } else {
                        moved = true;
                    }
                }
                float upRawX = motionEvent.getRawX();
                float upRawY = motionEvent.getRawY();

                float upDX = upRawX - downRawX;
                float upDY = upRawY - downRawY;

                if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    return true;
                } else {
                    moved = true;
                }
            }

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View) view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(layoutParams.leftMargin, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX); // Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(layoutParams.topMargin, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY); // Don't allow the FAB past the bottom of the parent

            saveCoordinates(newX, newY);

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            if (longClicked) {
                longClicked = false;
                return true;
            }

            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                return System.currentTimeMillis() - downTime >= 300 ? performLongClick() : performClick();
            } else {

                return true;
            }
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void setPositionEnsureVisibility(float newX, float newY) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        View viewParent = (View) getParent();
        int parentWidth = viewParent.getWidth();
        int parentHeight = viewParent.getHeight();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        newX = Math.max(layoutParams.leftMargin, newX); // Don't allow the FAB past the left hand side of the parent
        newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX); // Don't allow the FAB past the right hand side of the parent

        newY = Math.max(layoutParams.topMargin, newY); // Don't allow the FAB past the top of the parent
        newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY); // Don't allow the FAB past the bottom of the parent

        setX(newX);
        setY(newY);
    }

    public void bindRequiredData(@Nullable Display display, SharedPreferences postDetailsSharedPreferences, boolean portrait) {
        this.display = display;
        this.postDetailsSharedPreferences = postDetailsSharedPreferences;
        this.portrait = portrait;
    }

    public void setCoordinates() {
        if (postDetailsSharedPreferences == null) {
            return;
        }

        if (portrait) {
            if (postDetailsSharedPreferences.contains(SharedPreferencesUtils.getPostDetailFabPortraitX(display))
                    && postDetailsSharedPreferences.contains(SharedPreferencesUtils.getPostDetailFabPortraitY(display))) {
                setPositionEnsureVisibility(postDetailsSharedPreferences.getFloat(SharedPreferencesUtils.getPostDetailFabPortraitX(display), 0),
                        postDetailsSharedPreferences.getFloat(SharedPreferencesUtils.getPostDetailFabPortraitY(display), 0));
            }
        } else {
            if (postDetailsSharedPreferences.contains(SharedPreferencesUtils.getPostDetailFabLandscapeX(display))
                    && postDetailsSharedPreferences.contains(SharedPreferencesUtils.getPostDetailFabLandscapeY(display))) {
                setPositionEnsureVisibility(postDetailsSharedPreferences.getFloat(SharedPreferencesUtils.getPostDetailFabLandscapeX(display), 0),
                        postDetailsSharedPreferences.getFloat(SharedPreferencesUtils.getPostDetailFabLandscapeY(display), 0));
            }
        }
    }

    public void resetCoordinates() {
        if (portrait) {
            if (postDetailsSharedPreferences != null) {
                postDetailsSharedPreferences
                        .edit()
                        .remove(SharedPreferencesUtils.getPostDetailFabPortraitX(display))
                        .remove(SharedPreferencesUtils.getPostDetailFabPortraitY(display))
                        .apply();
            }
        } else {
            if (postDetailsSharedPreferences != null) {
                postDetailsSharedPreferences
                        .edit()
                        .remove(SharedPreferencesUtils.getPostDetailFabLandscapeX(display))
                        .remove(SharedPreferencesUtils.getPostDetailFabLandscapeY(display))
                        .apply();
            }
        }

        setTranslationX(0);
        setTranslationY(0);
    }

    private void saveCoordinates(float x, float y) {
        if (postDetailsSharedPreferences == null) {
            return;
        }

        if (portrait) {
            postDetailsSharedPreferences.edit().putFloat(SharedPreferencesUtils.getPostDetailFabPortraitX(display), x)
                    .putFloat(SharedPreferencesUtils.getPostDetailFabPortraitY(display), y)
                    .apply();
        } else {
            postDetailsSharedPreferences.edit().putFloat(SharedPreferencesUtils.getPostDetailFabLandscapeX(display), x)
                    .putFloat(SharedPreferencesUtils.getPostDetailFabLandscapeY(display), y)
                    .apply();
        }
    }
}
