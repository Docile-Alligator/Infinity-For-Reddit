package ml.docilealligator.infinityforreddit;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;

public interface FragmentCommunicator {
    default void refresh() {
    }

    default boolean handleKeyDown(int keyCode) {
        return false;
    }

    default void changeNSFW(boolean nsfw) {
    }

    default boolean startLazyMode() {
        return false;
    }

    default void stopLazyMode() {
    }

    default void resumeLazyMode(boolean resumeNow) {
    }

    default void pauseLazyMode(boolean startTimer) {
    }

    default boolean isInLazyMode() {
        return false;
    }

    default void changePostLayout(int postLayout) {
    }

    default void stopRefreshProgressbar() {
    }

    void applyTheme();

    default void hideReadPosts() {
    }

    default void changePostFilter(PostFilter postFilter) {
    }

    default PostFilter getPostFilter() {
        return null;
    }

    default void filterPosts() {

    }
}
