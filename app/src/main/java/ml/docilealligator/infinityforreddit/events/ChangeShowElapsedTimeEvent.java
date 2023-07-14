package ml.docilealligator.infinityforreddit.events;

public class ChangeShowElapsedTimeEvent {
    public boolean showElapsedTime;

    public ChangeShowElapsedTimeEvent(boolean showElapsedTime) {
        this.showElapsedTime = showElapsedTime;
    }
}
