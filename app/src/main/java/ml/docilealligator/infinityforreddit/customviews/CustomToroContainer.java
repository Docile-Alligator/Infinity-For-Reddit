package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import im.ene.toro.widget.Container;

public class CustomToroContainer extends Container {
    private OnWindowFocusChangedListener listener;

    public CustomToroContainer(Context context) {
        super(context);
    }

    public CustomToroContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToroContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (listener != null) {
            listener.onWindowFocusChanged(hasWindowFocus);
        }
    }

    public void addOnWindowFocusChangedListener(OnWindowFocusChangedListener onWindowFocusChangedListener) {
        this.listener = onWindowFocusChangedListener;
    }

    public interface OnWindowFocusChangedListener {
        void onWindowFocusChanged(boolean hasWindowsFocus);
    }
}
