package ml.docilealligator.infinityforreddit.events;

public class ChangeVibrateWhenActionTriggeredEvent {
    public boolean vibrateWhenActionTriggered;

    public ChangeVibrateWhenActionTriggeredEvent(boolean vibrateWhenActionTriggered) {
        this.vibrateWhenActionTriggered = vibrateWhenActionTriggered;
    }
}
