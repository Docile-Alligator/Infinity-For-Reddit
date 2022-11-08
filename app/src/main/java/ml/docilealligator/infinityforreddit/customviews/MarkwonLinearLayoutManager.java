package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

public class MarkwonLinearLayoutManager extends LinearLayoutManagerBugFixed {

    @Nullable
    private final SwipeLockScrollView.SwipeLockInterface swipeLockInterface;

    public MarkwonLinearLayoutManager(Context context,
                                      @Nullable SwipeLockScrollView.SwipeLockInterface swipeLockInterface) {
        super(context);
        this.swipeLockInterface = swipeLockInterface;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        if (child instanceof SwipeLockScrollView) {
            ((SwipeLockScrollView) child).setSwipeLockInterface(swipeLockInterface);
        }
    }
}
