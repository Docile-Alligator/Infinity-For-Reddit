package ml.ino6962.postinfinityforreddit.events;

public class ChangeHideTheNumberOfAwardsEvent {
    public boolean hideTheNumberOfAwards;

    public ChangeHideTheNumberOfAwardsEvent(boolean hideTheNumberOfAwards) {
        this.hideTheNumberOfAwards = hideTheNumberOfAwards;
    }
}
