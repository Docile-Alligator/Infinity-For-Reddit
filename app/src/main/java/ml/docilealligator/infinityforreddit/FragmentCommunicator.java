package ml.docilealligator.infinityforreddit;

public interface FragmentCommunicator {
    void refresh();

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

    default void changePostLayout(int postLayout) { }

}
