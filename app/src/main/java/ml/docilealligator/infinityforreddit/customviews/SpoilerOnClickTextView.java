package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class SpoilerOnClickTextView extends androidx.appcompat.widget.AppCompatTextView {
    private boolean isSpoilerOnClick;

    public SpoilerOnClickTextView(Context context) {
        super(context);
    }

    public SpoilerOnClickTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoilerOnClickTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isSpoilerOnClick() {
        return isSpoilerOnClick;
    }

    public void setSpoilerOnClick(boolean spoilerOnClick) {
        isSpoilerOnClick = spoilerOnClick;
    }
}
