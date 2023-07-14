package ml.ino6962.postinfinityforreddit.events;

public class ChangeSwipeActionThresholdEvent {
    public float swipeActionThreshold;

    public ChangeSwipeActionThresholdEvent(float swipeActionThreshold) {
        this.swipeActionThreshold = swipeActionThreshold;
    }
}
