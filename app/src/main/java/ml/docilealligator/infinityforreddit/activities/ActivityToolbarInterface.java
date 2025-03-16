package ml.docilealligator.infinityforreddit.activities;

public interface ActivityToolbarInterface {
    void onLongPress();
    default void displaySortType() {}
}
