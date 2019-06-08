package ml.docilealligator.infinityforreddit;

interface FragmentCommunicator {
    void refresh();
    default void startLazyMode() {};
    default void stopLazyMode() {};
}
