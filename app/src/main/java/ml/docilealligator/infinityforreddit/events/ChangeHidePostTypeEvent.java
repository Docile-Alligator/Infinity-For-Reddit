package ml.ino6962.postinfinityforreddit.events;

public class ChangeHidePostTypeEvent {
    public boolean hidePostType;

    public ChangeHidePostTypeEvent(boolean hidePostType) {
        this.hidePostType = hidePostType;
    }
}
