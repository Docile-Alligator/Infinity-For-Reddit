package ml.docilealligator.infinityforreddit;

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
}
