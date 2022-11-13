package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

public class SwipeLockLinearLayoutManager extends LinearLayoutManagerBugFixed {

    @Nullable
    private final SwipeLockInterface swipeLockInterface;

    public SwipeLockLinearLayoutManager(Context context,
                                        @Nullable SwipeLockInterface swipeLockInterface) {
        super(context);
        this.swipeLockInterface = swipeLockInterface;
    }

    public SwipeLockLinearLayoutManager(Context context, int orientation, boolean reverseLayout, @Nullable SwipeLockInterface swipeLockInterface) {
        super(context, orientation, reverseLayout);
        this.swipeLockInterface = swipeLockInterface;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        if (child instanceof SwipeLockView) {
            ((SwipeLockView) child).setSwipeLockInterface(swipeLockInterface);
        }
    }
}
