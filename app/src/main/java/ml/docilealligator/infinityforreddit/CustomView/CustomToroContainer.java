package ml.docilealligator.infinityforreddit.CustomView;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import im.ene.toro.widget.Container;

public class CustomToroContainer extends Container {
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
}
