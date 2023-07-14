package ml.docilealligator.infinityforreddit.events;

public class ChangeSwipeActionEvent {
    public int swipeLeftAction;
    public int swipeRightAction;

    public ChangeSwipeActionEvent(int swipeLeftAction, int swipeRightAction) {
        this.swipeLeftAction = swipeLeftAction;
        this.swipeRightAction = swipeRightAction;
    }
}
