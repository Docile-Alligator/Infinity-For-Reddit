package ml.docilealligator.infinityforreddit;

interface FragmentCommunicator {

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

}
