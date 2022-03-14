package ml.docilealligator.infinityforreddit;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    private AppBarStateChangeListener.State mCurrentState = AppBarStateChangeListener.State.IDLE;
    private int lastOffset = -1;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (lastOffset == verticalOffset) {
            return;
        }
        lastOffset = verticalOffset;

        if (verticalOffset == 0) {
            if (mCurrentState != AppBarStateChangeListener.State.EXPANDED) {
                onStateChanged(appBarLayout, AppBarStateChangeListener.State.EXPANDED);
            }
            mCurrentState = AppBarStateChangeListener.State.EXPANDED;
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != AppBarStateChangeListener.State.COLLAPSED) {
                onStateChanged(appBarLayout, AppBarStateChangeListener.State.COLLAPSED);
            }
            mCurrentState = AppBarStateChangeListener.State.COLLAPSED;
        } else {
            if (mCurrentState != AppBarStateChangeListener.State.IDLE) {
                onStateChanged(appBarLayout, AppBarStateChangeListener.State.IDLE);
            }
            mCurrentState = AppBarStateChangeListener.State.IDLE;
        }
    }

    /**
     * Notifies on state change
     *
     * @param appBarLayout Layout
     * @param state        Collapse state
     */
    public abstract void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state);

    // State
    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }
}
