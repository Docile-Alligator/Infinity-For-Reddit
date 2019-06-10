package ml.docilealligator.infinityforreddit;

interface FragmentCommunicator {
    void refresh();
    default void startLazyMode() {}
    default void stopLazyMode() {}
    default void resumeLazyMode(boolean resumeNow) {}
    default void pauseLazyMode(boolean startTimer) {}
}
