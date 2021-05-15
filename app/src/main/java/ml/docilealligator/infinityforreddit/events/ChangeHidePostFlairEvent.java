package ml.docilealligator.infinityforreddit.events;

public class ChangeHidePostFlairEvent {
    public boolean hidePostFlair;

    public ChangeHidePostFlairEvent(boolean hidePostFlair) {
        this.hidePostFlair = hidePostFlair;
    }
}
